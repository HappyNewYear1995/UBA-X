生成完代码后别执行mvn命令
## 1. 项目模块结构规范
### 1.1 核心业务包

- **`.controller`**:
    - 子包 `admin` (管理后台, URL `/module/...`) 和 `app` (用户端, URL `/module/...`)。
    - **VO 位置**: 同级包或 `.vo` 子包 (如 `...controller.admin.vo` ,`...controller.app.vo`)
      ,controller的请求参数和返回参数对象必须都放在对应的vo包中 。
    - 返回 `CommonResult`。**严禁业务逻辑**。
    - 存放 `XXXService` 接口与 `XXXServiceImpl` 实现。
- **`.convert`**:
    - 存放 MapStruct 接口 (`XXXConvert`)。负责 `DO` ↔ `VO`/`DTO` 转换。
- **`.dal` (数据访问层)**:
    - `dataobject`: 存放 `XXXDO` (继承 `BaseDO`, 加 `@TableName`)。
    - `mysql`: 存放 `XXXMapper` (继承 `BaseMapperX`, 加 `@Mapper`)。
- **`.config`**:
    - 存放 `@Configuration` 类 (MapperScan, MQ 配置等)。
- **`.api`** (仅 `xxx-api` 模块):
    - `XXXApiImpl`: 远程调用接口实现,实现`xxx-api`模块中的 `XXXApi` 参数/返回值必须用 `DTO`。

## 2. 统一返回结果

- **强制规则**: 所有 Controller 接口返回值必须是 `CommonResult<T>`。
- **成功响应**: `return CommonResult.success(data);`
- **失败响应**: 抛出 `ServiceException`，由全局异常处理器 `GlobalExceptionHandler` 捕获并转为 `CommonResult.error(...)`。
- **严禁行为**: 严禁直接返回 `Entity/DO`, 或自定义 POJO 作为 Controller 返回值。
- **分页规范**: 分页查询必须返回 `CommonResult<PageResult<T>>`，使用 `PageResult` 包装 MP 的 `IPage`。

## 3. 异常处理机制

- **业务异常**: 遇到业务校验失败或逻辑错误，必须抛出 `ServiceException` (具体包名依项目实际结构而定)。
- **错误码管理**:
    - 必须使用定义好的错误码常量 (如 `ErrorCodeConstants.USER_NOT_FOUND`)。
    - **严禁**硬编码字符串消息 (如 `throw new ServiceException("用户不存在")` 是禁止的)。
    - 格式:
      `import static com.jishulink.framework.common.exception.util.ServiceExceptionUtil.exception; throw exception(ErrorCodeConstants.USER_NOT_FOUND);`
- **全局捕获**: 不要在业务代码中随意 try-catch 吞掉异常，依赖框架的全局异常处理机制。

## 4. 数据库与 MyBatis Plus

- **查询构建**:
    - 使用 `LambdaQueryWrapperX`或`MPJLambdaWrapperX` 查询数据。
    - **严禁使用字符串字段名** (如 `.eq("status", 1)`)。
- **Mapper 继承**: 必须继承 `BaseMapperX<T>` 。

## 5. 代码风格

- **通用**:
    - 字段使用驼峰命名。
- **Lombok**:
    - VO/DO 必须使用 `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`。
    - DO 类必须加上 `@TableName("table_name")` 并继承 `BaseDO`。
- **文档注释**:
    - 类、接口、方法必须有 JavaDoc，说明功能、参数、返回值、作者。
    - 复杂业务逻辑必须包含行内注释。
- **魔法值**: 严禁出现魔法值，必须提取到 `ErrorCodeConstants`、枚举类或常量类中。

## 7. API 文档规范

- **Controller 标注**:
    - 类级别必须添加 `@Tag(name = "模块名", description = "模块描述")`。
    - 方法级别必须添加 `@Operation(summary = "接口简述", description = "详细描述")`。
- **XXXApi 标注**:
    - 类级别必须添加 `@Tag(name = "RPC服务 - 模块名", description = "模块描述")`。
    - 方法级别必须添加 `@Operation(summary = "接口简述", description = "详细描述")`。
- **参数标注**:
    - `VO` 、`DTO` 中的字段必须添加 `@Schema(description = "字段含义", example = "示例值")`。
    - 必填字段需配合 `@NotNull` 或 `@NotEmpty`。

## 8. 参数校验

- **基础校验**: `ReqVO` 字段必须添加 JSR-303 注解 (`@NotNull`, `@Size`, `@NotEmpty` 等)。
- **复杂校验**:
    - 如果涉及跨字段校验或数据库存在性校验，**不要**在 VO 中写死。
    - 应在 Controller 中标记 `@Validated`，并在 Service 层手动编写校验逻辑，抛出 `ServiceException`。
- **触发校验**: Controller 方法参数前必须加 `@Valid` 或 `@Validated`。

# 9. 开发工作流

1. **需求分析**: 确认所属模块 (如 `system`, `pay`, `member`, `infra`) 及接口类型 (Admin/App)。
2. **错误码定义**: 若涉及新业务异常，先在 `ErrorCodeConstants` 对应模块常量类中定义错误码。
3. **实体定义 (DO)**: 创建 `XXXDO`，继承 `BaseDO`，配置 `@TableName`。
4. **VO 定义**: 创建 `XXXReqVO` (含校验注解) 和 `XXXRespVO`。
5. **转换器定义**: 创建 `XXXConvert` (MapStruct)，定义 `INSTANCE` 单例。
6. **Service 实现**:
    - 使用 `BaseMapperX` 操作数据库。
    - 添加 `@Transactional`。
    - 遇到错误抛出 `ServiceException`。
    - 如果需要调用其他模块API,那么引用其他模块的API,然后在当前模块中的`..framework.rpc.config.RpcConfiguration`的注解
      `@EnableFeignClients`中增加引用。
7. **Controller 实现**:
    - 确定包路径 (`controller.admin` 或 `controller.app`)。
    - 定义 URL (`/module/...`)。
    - 添加 `@PreAuthorize` 权限注解。
    - 调用 Service 并返回 `CommonResult`。

# 10. 技术约束与禁止事项

- **不要** 创建自己的全局异常处理逻辑，依赖框架现有的。
- **不要** 在controller写业务逻辑。
- **不要** 使用 `System.out.println`，必须使用 `Slf4j` (`log.info`, `log.error`)。
- **优先参考**: 优先参考项目中 `system` 模块的现有代码风格 (如 `UserController`, `UserService`) 作为模板。
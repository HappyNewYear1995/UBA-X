package com.huanniankj.module.gather.framework.datasource.core;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * SQL 安全校验器
 * <p>
 * 使用 JSqlParser 解析 SQL 语句，拦截并禁止执行 DROP、TRUNCATE、ALTER 等高危 DDL 语句，
 * 仅允许 SELECT、INSERT、UPDATE、DELETE 等 DML 操作。
 *
 * @author zhaoff
 */
@Slf4j
@Component
public class SqlSecurityValidator {

    /**
     * 禁止执行的 SQL 关键字集合 (高危操作)
     */
    private static final Set<String> DANGEROUS_KEYWORDS = Set.of(
            "DROP", "TRUNCATE", "ALTER", "CREATE", "GRANT", "REVOKE",
            "EXEC", "EXECUTE", "CALL", "SHUTDOWN", "KILL"
    );

    /**
     * 校验 SQL 是否安全
     *
     * @param sql SQL 语句
     * @return 是否安全
     */
    public boolean isSafe(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }

        // 1. 快速检查：检查是否包含高危关键字
        String upperSql = sql.toUpperCase().trim();
        for (String keyword : DANGEROUS_KEYWORDS) {
            if (upperSql.startsWith(keyword)) {
                log.warn("SQL 安全校验失败，包含高危关键字: {}, sql={}", keyword, sql);
                return false;
            }
        }

        // 2. 使用 JSqlParser 解析 SQL 结构
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            // 仅允许 SELECT、INSERT、UPDATE、DELETE
            return statement instanceof Select
                    || statement instanceof Insert
                    || statement instanceof Update
                    || statement instanceof Delete;
        } catch (JSQLParserException e) {
            log.warn("SQL 解析失败，可能包含不安全操作: sql={}, error={}", sql, e.getMessage());
            return false;
        }
    }

    /**
     * 获取 SQL 语句类型
     *
     * @param sql SQL 语句
     * @return SQL 类型描述
     */
    public String getSqlType(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Select) {
                return "SELECT";
            } else if (statement instanceof Insert) {
                return "INSERT";
            } else if (statement instanceof Update) {
                return "UPDATE";
            } else if (statement instanceof Delete) {
                return "DELETE";
            } else if (statement instanceof Drop) {
                return "DROP";
            } else if (statement instanceof Truncate) {
                return "TRUNCATE";
            } else {
                return "OTHER";
            }
        } catch (JSQLParserException e) {
            return "PARSE_ERROR";
        }
    }

}

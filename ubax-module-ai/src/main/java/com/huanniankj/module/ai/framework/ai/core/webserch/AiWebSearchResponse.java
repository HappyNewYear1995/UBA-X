package com.huanniankj.module.ai.framework.ai.core.webserch;

import lombok.Data;

import java.util.List;

@Data
public class AiWebSearchResponse {

    /**
     * 总数（总共匹配的网页数）
     */
    private Long total;

    /**
     * 数据列表
     */
    private List<WebPage> lists;

    /**
     * 网页对象
     */
    @Data
    public static class WebPage {

        /**
         * 名称
         * <p>
         * 例如说：搜狐网
         */
        private String name;

        /**
         * 图标
         */
        private String icon;

        /**
         * 标题
         */
        private String title;

        /**
         * URL
         */
        @SuppressWarnings("JavadocLinkAsPlainText")
        private String url;

        /**
         * 内容的简短描述
         */
        private String snippet;

        /**
         * 内容的文本摘要
         */
        private String summary;

    }

}

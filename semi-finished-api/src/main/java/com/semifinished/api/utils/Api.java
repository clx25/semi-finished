package com.semifinished.api.utils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * api的代码创建方式
 * todo 规则太复杂，规划中，不一定写。。。
 */
@AllArgsConstructor
@NoArgsConstructor
public class Api {
    private String group;
    private String path;

    private String summary;

    private Params params;



    public static ApiBuilder builder() {
        return new ApiBuilder();
    }

    public static class ApiBuilder {
        private String group;
        private String path;
        private String summary;
        private Params.ParamsBuilder params;

        ApiBuilder() {
        }

        public ApiBuilder group(final String group) {
            this.group = group;
            return this;
        }

        public ApiBuilder path(final String path) {
            this.path = path;
            return this;
        }

        public ApiBuilder summary(final String summary) {
            this.summary = summary;
            return this;
        }

        public Params.ParamsBuilder params() {
            this.params = Params.builder();
            return this.params;
        }

        public Api build() {
            return new Api(this.group, this.path, this.summary, this.params.create());
        }

    }

    @AllArgsConstructor
    private static class Params {
        private Table table;

        private Sort sort;

        public static ParamsBuilder builder() {
            return new ParamsBuilder();
        }

        public static class ParamsBuilder extends ApiBuilder {
            private Table.TableBuilder table;

            private Sort.SortBuilder sort;


            public Table.TableBuilder table() {
                this.table = Table.builder();
                return this.table;
            }

            public Sort.SortBuilder sort() {
                this.sort = Sort.builder();
                return this.sort;
            }

            Params create() {
                return new Params(this.table.createTable(), this.sort.createSort());
            }

        }
    }




    @AllArgsConstructor
    private static class Table {
        private boolean distinct;

        private String name;

        public static Table.TableBuilder builder() {
            return new Table.TableBuilder();
        }

        public static class TableBuilder extends Params.ParamsBuilder {
            private boolean distinct;

            private String name;

            public TableBuilder distinct(boolean distinct) {
                this.distinct = distinct;
                return this;
            }

            public TableBuilder name(String name) {
                this.name = name;
                return this;
            }

            Table createTable() {
                return new Table(this.distinct, this.name);
            }

        }
    }

    @AllArgsConstructor
    private static class Sort {
        private boolean des;

        private String column;

        public static Sort.SortBuilder builder() {
            return new Sort.SortBuilder();
        }

        public static class SortBuilder extends Params.ParamsBuilder {
            private boolean des;

            private String column;

            public SortBuilder des(boolean des) {
                this.des = des;
                return this;
            }

            public SortBuilder column(String column) {
                this.column = column;
                return this;
            }
            Sort createSort() {
                return new Sort(this.des, this.column);
            }

        }
    }


}

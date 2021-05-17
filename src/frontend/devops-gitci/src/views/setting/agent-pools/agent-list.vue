<template>
    <article class="agent-list-home">
        <header class="agent-list-head">
            <bk-breadcrumb>
                <bk-breadcrumb-item v-for="(item,index) in navList" :key="index" :to="item.link">{{item.title}}</bk-breadcrumb-item>
            </bk-breadcrumb>
        </header>

        <main class="agent-list-main">
            <section>
                <bk-button theme="primary" class="add-agent" @click="goToAddAgent">Add Agent</bk-button>
            </section>
            <bk-table class="agent-table"
                :data="agentList"
                :pagination="pagination"
                :outer-border="false"
                :header-border="false"
                :header-cell-style="{ background: '#fafbfd' }"
                :height="tableHeight"
                @page-change="pageChange"
                @page-limit-change="pageLimitChange"
            >
                <bk-table-column label="IP" prop="ip" width="150"></bk-table-column>
                <bk-table-column label="Display name" prop="name" width="150"></bk-table-column>
                <bk-table-column label="Tags" prop="credentialType">
                    <template slot-scope="props">
                        <section class="agent-tags">
                            <bk-tag-input v-if="props.row.isEditing"
                                v-model="props.row.tags"
                                class="tag-input"
                                placeholder="输入框失焦结束"
                                :list="list"
                                :has-delete-icon="true"
                                :allow-create="true"
                                @blur="toggleShowEdit(props.row)">
                            </bk-tag-input>
                            <template v-else>
                                <bk-tag v-for="tag in props.row.tags" :key="tag" theme="info">{{tag}}</bk-tag>
                            </template>
                            <i class="bk-icon icon-edit2" @click="toggleShowEdit(props.row)"></i>
                        </section>
                    </template>
                </bk-table-column>
                <bk-table-column label="OS/Architecture" prop="credentialRemark" width="150"></bk-table-column>
                <bk-table-column label="Status" prop="credentialRemark" width="150"></bk-table-column>
                <bk-table-column label="操作" width="150" class-name="handler-btn">
                    <template slot-scope="props">
                        <span class="update-btn" @click="deleteAgent(props.row)">删除</span>
                    </template>
                </bk-table-column>
            </bk-table>
        </main>
    </article>
</template>

<script>
    import { mapState } from 'vuex'

    export default {
        data () {
            return {
                navList: [
                    { link: { name: 'agentPools' }, title: 'Agent Pools' },
                    { link: '', title: 'Agent List' }
                ],
                pagination: {
                    count: 0,
                    current: 1,
                    limit: 10
                },
                agentList: [{ tags: [] }],
                list: [{ id: '1', name: '2' }]
            }
        },

        computed: {
            ...mapState(['appHeight']),

            tableHeight () {
                return Math.min(this.appHeight - 152, 106 + (Math.max(this.agentList.length, 3)) * 42)
            }
        },

        methods: {
            pageChange () {},

            pageLimitChange () {},

            deleteAgent () {},

            addTag () {
                this.isAddTag = !this.isAddTag
            },

            toggleShowEdit (row) {
                this.$set(row, 'isEditing', !row.isEditing)
            },

            goToAddAgent () {
                this.$router.push({
                    name: 'addAgent'
                })
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .agent-list-head {
        height: 49px;
        line-height: 49px;
        background: #fff;
        box-shadow: 0 2px 5px 0 rgba(51,60,72,0.03);
        padding: 0 25.5px;
    }
    .agent-list-main {
        padding: 20px 24px;
        .add-agent {
            margin-bottom: 20px;
        }
    }
    .agent-table {
        .agent-tags {
            display: flex;
            align-items: center;
            .tag-input {
                width: 250px;
            }
            .icon-edit2 {
                display: none;
                font-size: 24px;
                cursor: pointer;
            }
        }
        /deep/ .hover-row .icon-edit2 {
            display: block;
        }
    }
</style>

<template>
    <section class="agent-pool-card">
        <header class="card-header">
            <h5 class="header-info">
                <span class="info-title">{{ pool.name }}</span>
                <span class="info-num" v-if="editable">Agent：{{ pool.nodeCount }}</span>
                <span class="info-num" v-else>Agent：{{ pool.enableNode }} / {{ pool.totalNode }}</span>
            </h5>

            <opt-menu v-if="editable">
                <li @click="goToAgentList">节点列表</li>
                <li @click="showDelete = true">删除</li>
            </opt-menu>
        </header>

        <ul class="card-useages" v-if="!editable">
            <li class="useage-item" v-for="usage in cpuUsages" :key="usage.name">
                <span class="item-header">
                    <span class="header-title">{{ usage.name }}</span>
                    <span class="header-val">{{ usage.showVal }}%</span>
                </span>
                <bk-progress :theme="getTheme(usage.val)" :percent="usage.val" :show-text="false"></bk-progress>
            </li>
        </ul>
        <bk-exception class="exception-wrap-item exception-part card-useages" type="empty" scene="part" v-else> </bk-exception>
        <bk-button @click="addAgent" class="card-button" v-if="editable">Add agent</bk-button>
        <bk-dialog v-model="showDelete"
            theme="danger"
            :mask-close="false"
            :loading="isDeleteing"
            @confirm="deletePool"
            title="确认删除">
            是否删除【{{pool.name}}】？
        </bk-dialog>
    </section>
</template>

<script>
    import optMenu from '@/components/opt-menu'
    import { setting } from '@/http'
    import { mapState } from 'vuex'

    export default {
        components: {
            optMenu
        },

        props: {
            editable: {
                type: Boolean,
                default: true
            },
            pool: Object
        },

        data () {
            return {
                showDelete: false,
                isDeleteing: false
            }
        },

        computed: {
            ...mapState(['projectId']),

            cpuUsages () {
                return [
                    { name: '构建机CPU负载', showVal: this.pool.averageCpuLoad, val: this.pool.averageCpuLoad / 100 },
                    { name: '构建机内存负载', showVal: this.pool.averageMemLoad, val: this.pool.averageMemLoad / 100 },
                    { name: '构建机硬盘负载', showVal: this.pool.averageDiskLoad, val: this.pool.averageDiskLoad / 100 }
                ]
            }
        },

        methods: {
            deletePool () {
                this.isDeleteing = true
                setting.deleteEnvironment(this.projectId, this.pool.envHashId).then(() => {
                    this.showDelete = false
                    this.$emit('refresh')
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isDeleteing = false
                })
            },

            addAgent () {
                this.$router.push({
                    name: 'addAgent',
                    params: {
                        poolId: this.pool.envHashId
                    }
                })
            },

            getTheme (val) {
                let theme = 'success'
                if (val > 0.5) {
                    theme = 'warning'
                }
                if (val > 0.8) {
                    theme = 'danger'
                }
                return theme
            },

            goToAgentList () {
                this.$router.push({ name: 'agentList', params: { poolId: this.pool.envHashId } })
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .agent-pool-card {
        width: 368px;
        height: 310px;
        background: #FFFFFF;
        border: 1px solid #dde4eb;
        border-radius: 2px;
    }
    .card-header {
        height: 75px;
        padding: 17px 10px 17px 24px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-bottom: 1px solid #dde4eb;
        .info-title {
            font-size: 16px;
            color: #333C48;
            line-height: 21px;
            margin-bottom: 4px;
            display: block;
        }
        .info-num {
            font-size: 12px;
            line-height:16px;
            color: #c3cdd7;
        }
    }
    .card-useages {
        padding: 7px 24px 24px;
        .useage-item {
            margin-top: 22px;
            .item-header {
                font-size: 12px;
                line-height: 14px;
                height: 14px;
                color: #7b7d8a;
                display: inline-block;
                width: 100%;
                margin-bottom: 7px;
            }
            .header-val {
                float: right;
                color: #979ba5;
            }
        }
    }
    .card-button {
        margin: 0 4px 0 24px;
        width: 320px;
        font-size: 12px;
    }
</style>

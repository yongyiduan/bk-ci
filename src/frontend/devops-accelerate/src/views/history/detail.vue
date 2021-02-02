<template>
    <article class="history-detail-home">
        <bk-breadcrumb separator-class="bk-icon icon-angle-right" class="bread-crumb">
            <bk-breadcrumb-item :to="{ name: 'history' }">历史列表</bk-breadcrumb-item>
            <bk-breadcrumb-item>{{ detail.elapsedTime }} 详情</bk-breadcrumb-item>
        </bk-breadcrumb>

        <section class="g-accelerate-box hisory-detail-data" v-bkloading="{ isloading }">
            <header class="detail-header">
                <span class="header-title">{{ detail.elapsedTime }}</span>
                <span class="header-time">
                    <span><span>开始时间：</span><span>{{ detail.startTime }}</span></span>
                    <span><span>总耗时：</span><span>{{ detail.elapsedTime }}</span></span>
                </span>
            </header>
            <bk-divider></bk-divider>
            <ul class="detail-list">
                <li class="task-item" v-for="task in detail.displayFields" :key="task.fieldName">
                    <span class="task-title">{{ task.fieldName }}:</span>
                    <span class="task-value">{{ task.fieldValue }}</span>
                    <logo name="tiaozhuan" prefix="" class="task-link" @click.native="openLink(task.linkAddress)" v-if="task.linkAddress"></logo>
                </li>
            </ul>
        </section>
    </article>
</template>

<script>
    import { getTurboRecord } from '@/api'
    import logo from '@/components/logo'

    export default {
        components: {
            logo
        },

        data () {
            return {
                detail: {},
                isloading: false
            }
        },

        created () {
            this.initData()
        },

        methods: {
            initData () {
                const id = this.$route.params.id
                this.isloading = true
                getTurboRecord(id).then((res) => {
                    this.detail = res || {}
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isloading = false
                })
            },

            openLink (linkAddress) {
                window.open(linkAddress, '_blank')
            }
        }
    }
</script>

<style lang="scss" scoped>
    .history-detail-home {
        padding: 10px 20px 28px;
        margin: 0 auto;
        .bread-crumb {
            font-size: 12px;
            margin-bottom: 10px;
            /deep/ .bk-breadcrumb-separator {
                font-size: 14px;
            }
            .bk-breadcrumb-item:last-child {
                color: #000;
            }
        }
    }

    .hisory-detail-data {
        padding: 25px 32px;
        .detail-header {
            line-height: 22px;
            margin-bottom: 17px;
            display: flex;
            .header-title {
                color: #000;
                flex: 1;
            }
            .header-time {
                flex: 1;
                color: #63656e;
                display: flex;
                align-items: center;
                >:first-child {
                    display: inline-block;
                    margin-right: 70px;
                }
            }
        }
        .detail-list {
            padding: 15px 0 16px;
            &::after {
                content: '';
                display: table;
                clear: both;
            }
            .task-item {
                width: 50%;
                float: left;
                line-height: 22px;
                margin-top: 16px;
                display: flex;
                align-items: center;
                .task-title {
                    display: inline-block;
                    width: 100px;
                    color: #979ba5;
                }
                .task-value {
                    color: #63656e;
                }
                .task-link {
                    color: #3a84ff;
                    margin-left: 11px;
                    cursor: pointer;
                }
            }
        }
    }

    /deep/ .bk-divider {
        font-size: 0;
    }
</style>

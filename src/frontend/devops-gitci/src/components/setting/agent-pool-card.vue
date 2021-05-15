<template>
    <section class="agent-pool-card">
        <header class="card-header">
            <h5 class="header-info">
                <span class="info-title">docker</span>
                <span class="info-num">Agent：100 / 100</span>
            </h5>

            <opt-menu v-if="editable">
                <li @click="goToAgentList">节点列表</li>
                <li>删除</li>
            </opt-menu>
        </header>

        <ul class="card-useages">
            <li class="useage-item">
                <span class="item-header">
                    <span class="header-title">CPU useage</span>
                    <span class="header-val">0.66%</span>
                </span>
                <bk-progress :theme="getTheme(0.2)" :percent="0.2" :show-text="false"></bk-progress>
            </li>
            <li class="useage-item">
                <span class="item-header">
                    <span class="header-title">CPU useage</span>
                    <span class="header-val">0.66%</span>
                </span>
                <bk-progress :theme="getTheme(0.6)" :percent="0.6" :show-text="false"></bk-progress>
            </li>
            <li class="useage-item">
                <span class="item-header">
                    <span class="header-title">CPU useage</span>
                    <span class="header-val">0.66%</span>
                </span>
                <bk-progress :theme="getTheme(0.9)" :percent="0.9" :show-text="false"></bk-progress>
            </li>
        </ul>
        <bk-button @click="addAgent" class="card-button" v-if="editable">Add agent</bk-button>
        <bk-button @click="addAgent" class="card-button" v-if="editable">关联</bk-button>
    </section>
</template>

<script>
    import optMenu from '@/components/opt-menu'

    export default {
        components: {
            optMenu
        },

        props: {
            editable: {
                type: Boolean,
                default: true
            }
        },

        methods: {
            addAgent () {
                this.$router.push({
                    name: 'addAgent',
                    params: {
                        pool: 'w'
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
                this.$router.push({ name: 'agentList' })
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
        width: 156px;
        font-size: 12px;
        +.card-button {
            margin: 0;
        }
    }
</style>

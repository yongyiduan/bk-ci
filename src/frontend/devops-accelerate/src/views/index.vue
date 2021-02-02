<template>
    <article class="accelerate-home">
        <bk-tab :active.sync="active" @tab-change="gotoPage" type="unborder-card" class="home-nav g-accelerate-box-without-radius">
            <bk-tab-panel v-for="(panel, index) in panels"
                v-bind="panel"
                :key="index">
            </bk-tab-panel>
        </bk-tab>
        <router-view class="accelerate-main"></router-view>
    </article>
</template>

<script>
    export default {
        data () {
            return {
                panels: [
                    { name: 'overview', label: '总览', count: 10 },
                    { name: 'task', label: '加速方案', count: 20 },
                    { name: 'history', label: '加速历史', count: 30 }
                ],
                active: 'overview'
            }
        },

        created () {
            window.addEventListener('resize', this.flexible, false)
            this.flexible()
            this.setDefaultActive()
        },

        beforeDestroy () {
            window.removeEventListener('resize', this.flexible, false)
            const doc = window.document
            const docEl = doc.documentElement
            docEl.style.fontSize = '14px'
        },

        methods: {
            setDefaultActive () {
                const name = this.$route.name
                const activeMap = {
                    overview: 'overview',
                    task: 'task',
                    taskInit: 'task',
                    taskDetail: 'task',
                    taskCreate: 'task',
                    taskList: 'task',
                    taskSuccess: 'task',
                    history: 'history',
                    historyDetail: 'history'
                }
                this.active = activeMap[name] || 'overview'
            },

            gotoPage (name) {
                this.$router.push({ name })
            },

            flexible () {
                const doc = window.document
                const docEl = doc.documentElement
                const designWidth = 1310 // 默认设计图宽度
                const maxRate = 1920 / designWidth
                const minRate = 1280 / designWidth
                const clientWidth = docEl.getBoundingClientRect().width || window.innerWidth
                const flexibleRem = Math.max(Math.min(clientWidth / designWidth, maxRate), minRate) * 100
                docEl.style.fontSize = flexibleRem + 'px'
            }
        }
    }
</script>

<style lang="scss" scoped>
    .accelerate-main {
        height: calc(94.04vh - 50px);
        max-width: 13.1rem;
        overflow: auto;
    }
    .home-nav {
        display: flex;
        align-items: center;
        justify-content: center;
        /deep/ .bk-tab-header {
            background-color: #fff;
            height: 5.96vh;
            line-height: 5.96vh;
            background-image: none;
            .bk-tab-label-wrapper .bk-tab-label-list {
                height: 5.96vh;
                .bk-tab-label-item {
                    line-height: 5.96vh;
                    height: 5.96vh;
                    color: #63656e;
                    &::after {
                        height: 3px;
                    }
                    &.active {
                        color: #3a84ff;
                    }
                    .bk-tab-label {
                        font-size: 16px;
                    }
                }
            }
            .bk-tab-header-setting {
                height: 5.96vh;
                line-height: 5.96vh;
            }
        }
        /deep/ .bk-tab-section {
            padding: 0;
        }
    }
</style>

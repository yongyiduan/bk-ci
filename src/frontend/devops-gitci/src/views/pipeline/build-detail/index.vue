<template>
    <article class="build-detail-home">
        <header class="build-detail-header">
            <bk-breadcrumb class="header-bread">
                <bk-breadcrumb-item v-for="(item,index) in list" :key="index" :to="item.link">{{item.title}}</bk-breadcrumb-item>
            </bk-breadcrumb>
            <bk-tab :active.sync="active" type="unborder-card" class="header-tab" @tab-change="changeTab">
                <bk-tab-panel
                    v-for="(panel, index) in panels"
                    v-bind="panel"
                    :key="index">
                </bk-tab-panel>
            </bk-tab>
        </header>
        <router-view class="build-detail-main"></router-view>
    </article>
</template>

<script>
    export default {
        data () {
            return {
                list: [
                    { title: 'Frontent CI', link: { name: 'buildList' } },
                    { title: '# 127' }
                ],
                panels: [
                    { label: '详情', name: 'buildDetail' },
                    { label: '构建', name: 'buildArtifacts' },
                    { label: '报告', name: 'buildReports' },
                    { label: '配置', name: 'buildConfig' }
                ],
                active: 'buildDetail'
            }
        },

        watch: {
            '$route.name': {
                handler (name) {
                    this.active = name
                    this.changeTab(name)
                },
                immediate: true
            }
        },

        methods: {
            changeTab (name) {
                this.$router.push({ name })
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .build-detail-header {
        background: #fff;
        box-shadow: 0 2px 5px 0 rgba(51,60,72,0.03);
        .header-bread {
            line-height: 41px;
            padding: 0 25px;
        }
        .header-tab {
            /deep/ .bk-tab-header {
                padding: 0 14px;
            }
            /deep/ .bk-tab-section {
                padding: 0;
            }
        }
    }
    .build-detail-main {
        margin: 25px;
        height: calc(100vh - 209px);
        overflow: auto;
        background: #fff;
    }
</style>

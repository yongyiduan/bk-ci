<template>
    <article class="task-success-home">
        <bk-breadcrumb separator-class="bk-icon icon-angle-right" class="bread-crumb">
            <bk-breadcrumb-item :to="{ name: 'taskList' }">方案列表</bk-breadcrumb-item>
            <bk-breadcrumb-item>新增加速方案</bk-breadcrumb-item>
        </bk-breadcrumb>

        <section class="g-accelerate-box" v-bkloading="{ isloading }">
            <p class="success-tip">
                <i class="bk-icon check"></i>
                <span class="g-accelerate-black-font">加速方案提交成功</span>
            </p>

            <section class="g-accelerate-task-tip" v-html="engineDetail.userManual"></section>
        </section>

        <bk-button theme="primary" @click="goToList">关闭</bk-button>
    </article>
</template>

<script>
    import { getEngineDetail } from '@/api'

    export default {
        data () {
            return {
                isloading: false,
                engineDetail: {}
            }
        },

        created () {
            this.initData()
        },

        methods: {
            initData () {
                this.isloading = true
                const engineCode = this.$route.query.engineCode
                getEngineDetail(engineCode).then((res = {}) => {
                    this.engineDetail = res || {}
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isloading = false
                })
            },

            goToList () {
                this.$router.push({
                    name: 'taskList'
                })
            }
        }
    }
</script>

<style lang="scss" scoped>
    @import '../../assets/scss/var.scss';
    h5 {
        font-weight: normal;
        font-size: 14px;
    }
    .task-success-home {
        padding: 10px 20px;
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
        .g-accelerate-box {
            margin-bottom: 20px;
        }
    }
    .success-tip {
        height: round(90px * $designToPx);
        padding: 0 .32rem;
        line-height: round(90px * $designToPx);
        font-size: 20px;
        border-bottom: 1px solid #f0f1f5;
    }
    .task-tip {
        padding: 26px 96px 34px;
    }
</style>

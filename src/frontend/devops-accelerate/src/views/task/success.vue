<template>
    <article class="task-success-home">
        <bk-breadcrumb separator-class="bk-icon icon-angle-right" class="bread-crumb">
            <bk-breadcrumb-item :to="{ name: 'taskList' }"> {{ $t('accelerate.方案列表') }} </bk-breadcrumb-item>
            <bk-breadcrumb-item> {{ $t('accelerate.新增加速方案') }} </bk-breadcrumb-item>
        </bk-breadcrumb>

        <section class="g-accelerate-box" v-bkloading="{ isloading }">
            <p class="success-tip">
                <logo name="check-circle" size="48" class="icon-success"></logo>
                <span class="g-accelerate-black-font"> {{ $t('accelerate.加速方案提交成功') }} </span>
                <span class="success-plan-id g-accelerate-gray-font"> {{ $t('accelerate.方案Id为：') }} {{ $route.query.planId }}<logo name="copy" class="icon-copy" size="16" @click.native="copy"></logo></span>
            </p>

            <section class="g-accelerate-task-tip success-tip-user" v-html="engineDetail.userManual"></section>
        </section>

        <bk-button theme="primary" @click="goToList"> {{ $t('accelerate.关闭') }} </bk-button>
    </article>
</template>

<script>
    import { getEngineDetail } from '@/api'
    import { copy } from '@/assets/js/util'
    import logo from '@/components/logo'

    export default {
        components: {
            logo
        },

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

            copy () {
                copy(this.$route.query.planId)
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
        font-size: 20px;
        border-bottom: 1px solid #f0f1f5;
        display: flex;
        align-items: center;
        .icon-success {
            color: #3FC06D;
        }
        .g-accelerate-black-font {
            margin-left: .15rem;
        }
        .success-plan-id {
            font-size: 14px;
            display: flex;
            align-items: flex-end;
            height: 24px;
            margin-left: 10px;
            line-height: 16px;
            .icon-copy {
                cursor: pointer;
                margin-left: 4px;
            }
        }
    }
    .success-tip-user {
        padding: 26px calc(.47rem + 48px) 34px;
    }
</style>

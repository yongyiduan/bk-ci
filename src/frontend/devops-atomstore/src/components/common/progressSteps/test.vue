<template>
    <section class="main-body">
        <section class="progress-test">
            <img src="../../../images/progressTest.png" class="test-image">
            <bk-button class="test-button" @click="goToTest"> {{ $t('store.前往测试') }} </bk-button>
            <span class="test-tip"> {{ $t('store.完成测试后返回此页面继续发布流程') }} </span>
        </section>

        <footer class="main-footer">
            <bk-button theme="primary" @click="completeTest" :loading="isLoading"> {{ $t('store.测试完成') }} </bk-button>
            <bk-button @click="previousStep"> {{ $t('store.上一步') }} </bk-button>
        </footer>
    </section>
</template>

<script>
    export default {
        props: {
            currentStep: {
                type: Object
            },
            detail: {
                type: Object
            }
        },

        data () {
            return {
                isLoading: false
            }
        },

        methods: {
            goToTest () {
                window.open(`/console/pipeline/${this.detail.projectCode}/list`, '_blank')
            },

            previousStep () {
                this.$parent.currentStepIndex--
            },

            completeTest () {
                this.isLoading = true
                this.$store.dispatch('store/requestServicePassTest', this.detail.serviceId).then(() => {
                    this.$emit('freshProgress')
                    this.$parent.currentStepIndex++
                }).catch((err) => {
                    this.$bkMessage({ message: err.message || err, theme: 'error' })
                }).finally(() => {
                    this.isLoading = false
                })
            }
        }
    }
</script>

<style lang="scss" scoped>
    @import '@/assets/scss/conf.scss';

    .progress-test {
        display: flex;
        justify-content: center;
        align-items: center;
        flex-direction: column;
        height: 100%;
        .test-image {
            width: 160px;
            height: 123px;
        }
        .test-button {
            margin: 24px 0 16px;
        }
        .test-tip {
            color: $grayBlack;
        }
    }
</style>

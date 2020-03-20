<template>
    <section class="main-body">
        <section class="progress-test">
            <img src="../../../images/progressTest.png" class="test-image">
            <bk-button class="test-button" @click="goToTest"> {{ $t('store.前往测试') }} </bk-button>
            <span class="test-tip" v-html="$t('store.testTip')"></span>
        </section>

        <footer class="main-footer">
            <bk-button theme="primary" @click="completeTest" :loading="isLoading"> {{ $t('store.测试完成') }} </bk-button>
            <bk-button :loading="isRebuildLoading" @click="rebuild"> {{ $t('store.重新构建') }} </bk-button>
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
                isLoading: false,
                isRebuildLoading: false
            }
        },

        methods: {
            goToTest () {
                window.open(`/console/pipeline/${this.detail.projectCode}/list`, '_blank')
            },

            rebuild () {
                this.isRebuildLoading = true
                const postData = {
                    id: this.detail.serviceId,
                    projectCode: this.detail.projectCode
                }
                this.$store.dispatch('store/requestRebuildService', postData).then(() => {
                    this.$emit('freshProgress')
                }).catch((err) => {
                    this.$bkMessage({ message: err.message || err, theme: 'error' })
                }).finally(() => {
                    this.isRebuildLoading = false
                })
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
            color: #b0b0b0;
        }
    }
</style>

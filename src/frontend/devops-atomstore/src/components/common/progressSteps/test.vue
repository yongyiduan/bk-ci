<template>
    <section class="main-body">
        <section class="progress-test">
            <img src="../../../images/progressTest.png" class="test-image">
            <bk-button class="test-button"> {{ $t('store.前往测试') }} </bk-button>
            <span class="test-tip"> {{ $t('store.完成测试后返回此页面继续发布流程') }} </span>
        </section>

        <footer class="main-footer">
            <bk-button theme="primary" @click="nextStep"> {{ $t('store.下一步') }} </bk-button>
            <bk-button @click="previousStep"> {{ $t('store.上一步') }} </bk-button>
        </footer>
    </section>
</template>

<script>
    export default {
        props: {
            currentStep: {
                type: Object
            }
        },

        methods: {
            previousStep () {
                this.$parent.currentStepIndex--
            },

            nextStep () {
                this.$parent.currentStepIndex++
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

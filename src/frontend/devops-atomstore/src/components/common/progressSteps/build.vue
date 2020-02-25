<template>
    <section class="main-body">
        <ul class="progress-log">
            <li class="log-item" v-for="(log, index) in logs" :key="index"> [{{index + 1}}] - {{log}} </li>
        </ul>

        <footer class="main-footer">
            <bk-button :disabled="currentStep.status === 'success'" theme="primary" @click=" nextStep"> {{ $t('store.下一步') }} </bk-button>
            <bk-button @click="previousStep"> {{ $t('store.重新构建') }} </bk-button>
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

        data () {
            return {
                logs: [
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh',
                    'aksdjashkdjsahdkjsadhkjsadhasjkdhakjdssahdjksadkjsahdakjshdakjsdh'
                ]
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

    .progress-log {
        background: #f5f5f5;
        border: 1px solid #d9d9d9;
        height: 100%;
        width: 100%;
        overflow: auto;
        padding: 16px;
        .log-item {
            width: 100%;
            word-break: break-all;
            font-size: 12px;
            line-height: 22px;
            color: $grayBlack;
        }
    }
</style>

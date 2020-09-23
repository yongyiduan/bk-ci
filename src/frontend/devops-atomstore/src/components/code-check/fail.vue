<template>
    <section>
        <p class="code-check-tip"><i class="bk-icon icon-info-circle"></i>代码检查任务执行失败，请点击重新检查进行重试，或联系CodeCC助手</p>
        <section class="code-ckeck-status">
            <i class="bk-icon icon-exclamation-circle status-icon"></i>
            <section class="code-check-summary">
                <h3 class="summary-head">代码检查任务失败</h3>
                <h5 class="summary-desc">{{ message }}</h5>
            </section>
            <bk-button :theme="isInDetailPage ? 'default' : 'primary'" class="code-check-button" :disabled="startChecking" @click="goToCodecc">查看详情</bk-button>
            <bk-button theme="primary" v-if="isInDetailPage" class="code-check-button" @click="startCodeCC">重新检查</bk-button>
        </section>
    </section>
</template>

<script>
    export default {
        props: {
            codeccUrl: String,
            message: String
        },

        data () {
            return {
                startChecking: false
            }
        },

        computed: {
            isInDetailPage () {
                return this.$route.name === 'check'
            }
        },

        methods: {
            startCodeCC () {
                this.startChecking = true
                this.$emit('startCodeCC').finally(() => {
                    this.startChecking = false
                })
            },

            goToCodecc () {
                window.open(this.codeccUrl, '_blank')
            }
        }
    }
</script>

<style lang="scss" scoped>
    .icon-exclamation-circle {
        color: #ea3636;
    }
    .code-check-button {
        margin-left: 10px;
    }
</style>

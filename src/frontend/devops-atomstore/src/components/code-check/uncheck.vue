<template>
    <section class="code-ckeck-status">
        <i class="bk-icon icon-info-circle status-icon" v-if="isInDetailPage"></i>
        <i class="bk-icon icon-exclamation-circle status-icon" v-else></i>
        <section class="code-check-summary" v-if="isInDetailPage">
            <h3 class="summary-head">未进行过检查</h3>
            <h5 class="summary-desc">使用腾讯代码分析检查，将从代码缺陷、安全漏洞、代码规范、圈复杂度、重复率等角度进行扫描，为产品质量保驾护航</h5>
        </section>
        <section class="code-check-summary" v-else>
            <h3 class="summary-head">代码检查失败</h3>
            <h5 class="summary-desc">{{ message }}</h5>
        </section>
        <bk-button theme="primary" class="code-check-button" @click="startCodeCC" :loading="startChecking">开始检查</bk-button>
    </section>
</template>

<script>
    export default {
        props: {
            startChecking: Boolean,
            message: String
        },

        computed: {
            isInDetailPage () {
                return this.$route.name === 'check'
            }
        },

        methods: {
            startCodeCC () {
                this.$emit('startCodeCC')
            }
        }
    }
</script>

<style lang="scss" scoped>
    .icon-info-circle {
        color: #c4c6cc;
    }
    .icon-exclamation-circle {
        color: #ea3636;
    }
</style>

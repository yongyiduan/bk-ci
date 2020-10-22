<template>
    <section class="code-ckeck-status">
        <i class="bk-icon icon-check-circle status-icon"></i>
        <section class="code-check-summary">
            <h3 class="summary-head">代码质量合格</h3>
            <h5 class="summary-desc">代码安全、代码规范、代码度量评分均 &gt;= 90 为合格，其中任一项 &lt; 90 即不合格</h5>
            <h5 class="summary-link" v-if="isInDetailPage">最近检查:<span class="link-txt" @click="goToLink(repoUrl)">{{ commitId }}</span>2019-02-15 14:32:49 <span class="link-txt" @click="goToLink(codeccUrl)">查看详情</span></h5>
        </section>
        <bk-button theme="primary" class="code-check-button" :disabled="startChecking" @click="startCodeCC" v-if="isInDetailPage">重新检查</bk-button>
        <bk-button theme="primary" class="code-check-button" @click="goToLink(codeccUrl)" v-else>查看详情</bk-button>
    </section>
</template>

<script>
    export default {
        props: {
            codeccUrl: String,
            commitId: String,
            repoUrl: String,
            startChecking: Boolean
        },

        computed: {
            isInDetailPage () {
                return this.$route.name === 'check'
            }
        },

        methods: {
            startCodeCC () {
                this.$emit('startCodeCC')
            },

            goToLink (url) {
                window.open(url, '_blank')
            }
        }
    }
</script>

<style lang="scss" scoped>
    .icon-check-circle {
        color: #3fc06d;
    }
</style>

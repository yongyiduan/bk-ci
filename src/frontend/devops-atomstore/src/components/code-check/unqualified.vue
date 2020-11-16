<template>
    <section>
        <p class="code-check-tip" v-if="!isInDetailPage"><i class="bk-icon icon-info-circle"></i>请点击 查看详情，确认代码检查发现的问题， 修改后，再次构建，再次测试</p>
        <section class="code-ckeck-status">
            <i class="bk-icon icon-exclamation-circle status-icon"></i>
            <section class="code-check-summary">
                <h3 class="summary-head">代码质量不合格</h3>
                <h5 class="summary-desc">{{ `代码安全 &gt;= ${ codeSecurityQualifiedScore } 分，代码规范 &gt;= ${ codeStyleQualifiedScore } 分，代码度量 &gt;= ${ codeMeasureQualifiedScore } 分时合格` }}</h5>
                <h5 class="summary-link" v-if="isInDetailPage">最近检查:<span class="link-txt" @click="goToLink(repoUrl)">{{ commitId }}</span>2019-02-15 14:32:49 <span class="link-txt" @click="goToLink(codeccUrl)">查看详情</span></h5>
            </section>
            <bk-button theme="primary" class="code-check-button" v-if="isInDetailPage" @click="startCodeCC" :loading="startChecking">重新检查</bk-button>
            <bk-button theme="primary" class="code-check-button" v-else @click="goToLink(codeccUrl)">查看详情</bk-button>
        </section>
    </section>
</template>

<script>
    export default {
        props: {
            codeccUrl: String,
            commitId: String,
            repoUrl: String,
            startChecking: Boolean,
            codeStyleQualifiedScore: String,
            codeSecurityQualifiedScore: String,
            codeMeasureQualifiedScore: String
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
    .icon-exclamation-circle {
        color: #ea3636;
    }
</style>

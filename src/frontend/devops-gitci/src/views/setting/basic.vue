<template>
    <article class="setting-basic-home" v-bkloading="{ isLoading }">
        <section class="basic-main">
            <h5 class="main-title">Config listening events</h5>
            <section class="main-checkbox">
                <bk-checkbox v-model="form.buildPushedBranches" class="checkbox">Build pushed branches</bk-checkbox>
                <bk-checkbox v-model="form.buildPushedPullRequest" class="checkbox">Build pushed merge request</bk-checkbox>
            </section>

            <h5 class="main-title">Config merge request</h5>
            <section class="main-checkbox">
                <bk-checkbox v-model="form.enableMrBlock" class="checkbox">Lock MR merge</bk-checkbox>
            </section>

            <h5 class="main-title">Config enable</h5>
            <section class="main-checkbox">
                <bk-checkbox v-model="form.enableCi" class="checkbox">Enable CI</bk-checkbox>
            </section>
        </section>

        <bk-button theme="primary" class="basic-btn" @click="saveSetting" :loading="isSaving">保存</bk-button>
    </article>
</template>

<script>
    import { setting } from '@/http'
    import { mapState } from 'vuex'

    export default {
        data () {
            return {
                form: {
                    projectId: this.projectId
                },
                isSaving: false,
                isLoading: false
            }
        },

        computed: {
            ...mapState(['projectId'])
        },

        created () {
            this.getSetting()
        },

        methods: {
            getSetting () {
                this.isLoading = true
                setting.getSetting().then((res) => {
                    const data = res.data
                    Object.assign(this.form, data)
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                })
            },

            saveSetting () {
                this.isSaving = true
                setting.saveSetting(this.form).then(() => {
                    this.$bkMessage({ theme: 'success', message: '保存成功' })
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isSaving = false
                })
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .setting-basic-home {
        padding: 20px 24px;
    }
    .basic-main {
        background: #fff;
        overflow: hidden;
        padding: 0 24px;
        .main-title {
            margin: 25px 0 18px;
            font-size: 16px;
            color: #313328;
        }
        .main-checkbox {
            padding-bottom: 25px;
            &:not(:last-child) {
                border-bottom: 1px solid #f0f1f5;
            }
        }
        .checkbox:not(:last-child) {
            display: block;
            margin-bottom: 17px;
        }
    }
    .basic-btn {
        margin-top: 20px;
        width: 88px;
    }
</style>

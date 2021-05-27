<template>
    <bk-sideslider :is-show.sync="show" :quick-close="true" @hidden="hidden" :width="622" title="add pool">
        <bk-form :model="formData" ref="poolForm" slot="content" class="pool-form" form-type="vertical">
            <bk-form-item label="名称" :required="true" :rules="[requireRule('名称'), nameRule]" property="name" error-display-type="normal">
                <bk-input v-model="formData.name" placeholder="在 runs-on 关键字下使用，以英文字母、数字、中划线组成，不超过64个字"></bk-input>
            </bk-form-item>
            <bk-form-item label="描述" property="desc">
                <bk-input type="textarea" v-model="formData.desc" placeholder="请输入描述"></bk-input>
            </bk-form-item>
            <bk-form-item>
                <bk-button ext-cls="mr5" theme="primary" title="提交" @click.stop.prevent="submitData" :loading="isSaving">提交</bk-button>
                <bk-button ext-cls="mr5" title="取消" @click="hidden" :disabled="isSaving">取消</bk-button>
            </bk-form-item>
        </bk-form>
    </bk-sideslider>
</template>

<script>
    import { setting } from '@/http'
    import { mapState } from 'vuex'

    export default {
        props: {
            show: Boolean
        },

        data () {
            return {
                formData: {
                    name: '',
                    desc: '',
                    envType: 'BUILD',
                    source: 'EXISTING',
                    nodeHashIds: []
                },
                nameRule: {
                    validator: (val) => (/^[a-zA-Z0-9-]{1,64}$/.test(val)),
                    message: '以英文字母、数字或中划线(-)组成，不超过64个字',
                    trigger: 'blur'
                },
                isSaving: false
            }
        },

        computed: {
            ...mapState(['projectId'])
        },

        watch: {
            show (val) {
                if (val) {
                    this.initData()
                }
            }
        },

        methods: {
            initData () {
                this.formData.name = ''
                this.formData.desc = ''
            },

            requireRule (name) {
                return {
                    required: true,
                    message: name + '是必填项',
                    trigger: 'blur'
                }
            },

            submitData () {
                this.$refs.poolForm.validate(() => {
                    this.isSaving = true
                    setting.addEnvironment(this.projectId, this.formData).then(() => {
                        this.hidden()
                        this.$emit('refresh')
                        this.$bkMessage({ theme: 'success', message: '添加成功' })
                    }).catch((err) => {
                        this.$bkMessage({ theme: 'error', message: err.message || err })
                    }).finally(() => {
                        this.isSaving = false
                    })
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.content || err })
                })
            },

            hidden () {
                this.$emit('update:show', false)
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .pool-form {
        padding: 20px 30px;
        /deep/ button {
            margin: 8px 10px 0 0;
        }
    }
</style>

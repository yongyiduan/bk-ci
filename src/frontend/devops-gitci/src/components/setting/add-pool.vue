<template>
    <bk-sideslider :is-show.sync="show" :quick-close="true" @hidden="hidden" :width="622" title="add pool">
        <bk-form :model="formData" ref="poolForm" slot="content" class="pool-form" form-type="vertical">
            <bk-form-item label="名称" :required="true" :rules="[requireRule('名称'), nameRule]" property="name" error-display-type="normal">
                <bk-input v-model="formData.name" placeholder="在 runs-on 关键字下使用，以英文字母、数字、中划线组成，不超过64个字"></bk-input>
            </bk-form-item>
            <bk-form-item label="描述" property="remark">
                <bk-input type="textarea" v-model="formData.remark" placeholder="请输入描述"></bk-input>
            </bk-form-item>
            <bk-form-item>
                <bk-button ext-cls="mr5" theme="primary" title="提交" @click.stop.prevent="submitData">提交</bk-button>
                <bk-button ext-cls="mr5" title="取消" @click="hidden">取消</bk-button>
            </bk-form-item>
        </bk-form>
    </bk-sideslider>
</template>

<script>
    export default {
        props: {
            show: Boolean
        },

        data () {
            return {
                formData: {
                    name: '',
                    remark: ''
                },
                nameRule: {
                    validator: (val) => (/^[a-zA-Z0-9-]{1,64}$/.test(val)),
                    message: '以英文字母、数字或中划线(-)组成，不超过64个字',
                    trigger: 'blur'
                }
            }
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
                this.formData.remark = ''
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
                    console.log(this.formData)
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

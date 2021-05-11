<template>
    <bk-sideslider :is-show.sync="show" :quick-close="true" @hidden="hidden" :width="622" :title="isEdit ? '编辑凭据' : '新增凭据'">
        <bk-form :model="formData" ref="credentialForm" slot="content" class="credential-form" form-type="vertical">
            <bk-form-item label="类型" :required="true" property="credentialType" :desc="computedTicket.desc" error-display-type="normal">
                <bk-select v-model="formData.credentialType" :clearable="false" @change="changeCredentialType" :disabled="isEdit">
                    <bk-option v-for="option in ticketTypes"
                        :key="option.id"
                        :id="option.id"
                        :name="option.name">
                    </bk-option>
                </bk-select>
            </bk-form-item>
            <bk-form-item label="名称" :required="true" :rules="[requireRule('名称'), idRule]" property="credentialId" :desc="customDesc" error-display-type="normal">
                <bk-input v-model="formData.credentialId" placeholder="以英文字母、数字或下划线(_)组成，不超过40个字" :disabled="isEdit"></bk-input>
            </bk-form-item>
            <bk-form-item label="别名" property="credentialName" :rules="[nameRule]" desc="在流水线中通过变量引用凭证时，仅支持通过名称引用" error-display-type="normal">
                <bk-input v-model="formData.credentialName" placeholder="以汉字、英文字母、数字、连字符(-)、下划线(_)或英文句号组成，不超过30个字"></bk-input>
            </bk-form-item>
            <bk-form-item :label="com.label" :required="com.required" :property="com.id" v-for="com in computedTicket.content" :key="com.id" error-display-type="normal" :rules="com.rules">
                <bk-input v-model="formData[com.id]" :type="com.type" :placeholder="com.placeholder"></bk-input>
            </bk-form-item>
            <bk-form-item label="描述" property="credentialRemark">
                <bk-input type="textarea" v-model="formData.credentialRemark" placeholder="请输入凭据描述"></bk-input>
            </bk-form-item>
            <bk-form-item>
                <bk-button ext-cls="mr5" theme="primary" title="提交" @click.stop.prevent="submitData" :loading="isLoading">提交</bk-button>
                <bk-button ext-cls="mr5" title="取消" @click="hidden" :disabled="isLoading">取消</bk-button>
            </bk-form-item>
        </bk-form>
    </bk-sideslider>
</template>

<script>
    import { setting } from '@/http'

    export default {
        props: {
            show: Boolean,
            form: Object
        },

        data () {
            return {
                ticketTypes: [
                    {
                        id: 'PASSWORD',
                        name: 'password',
                        desc: '用于蓝盾平台中需要加密保存的信息，如证书密码、脚本中需要加密字段等',
                        content: [
                            { id: 'v1', label: '密码', type: 'password', required: true, rules: [this.requireRule('密码')], placeholder: '请输入密码' }
                        ]
                    },
                    {
                        id: 'MULTI_LINE_PASSWORD',
                        name: 'multiLinePassword',
                        desc: '用于蓝盾平台中需要加密保存的信息，如证书密码、脚本中需要加密字段等',
                        content: [
                            { id: 'v1', label: '密码', type: 'textarea', required: true, rules: [this.requireRule('密码')], placeholder: '请输入密码' }
                        ]
                    },
                    {
                        id: 'USERNAME_PASSWORD',
                        name: 'usernamePassword',
                        desc: '用于蓝盾平台中需要加密保存的信息，如证书密码、脚本中需要加密字段等',
                        content: [
                            { id: 'v1', label: '用户名', type: 'text', placeholder: '请输入用户名' },
                            { id: 'v2', label: '密码', type: 'password', required: true, rules: [this.requireRule('密码')], placeholder: '请输入密码' }
                        ]
                    },
                    {
                        id: 'ACCESSTOKEN',
                        name: 'accessToken',
                        desc: '一个访问令牌包含了此登陆会话的安全信息，用于关联Gitlab类型代码库，',
                        content: [
                            { id: 'v1', label: 'AccessToken', type: 'password', required: true, rules: [this.requireRule('AccessToken')], placeholder: '请输入AccessToken' }
                        ]
                    },
                    {
                        id: 'SECRETKEY',
                        name: 'secretKey',
                        desc: '用于蓝盾平台中需要加密保存的信息，如证书密码、脚本中需要加密字段等',
                        content: [
                            { id: 'v1', label: 'secretKey', type: 'password', required: true, rules: [this.requireRule('secretKey')], placeholder: '请输入secretKey' }
                        ]
                    },
                    {
                        id: 'APPID_SECRETKEY',
                        name: 'appIdSecretKey',
                        desc: '用来设置key value的键值对类型，例如bugly原子要填的用户帐号密码、api调用等',
                        content: [
                            { id: 'v1', label: 'appId', type: 'text', required: true, rules: [this.requireRule('appId')], placeholder: '请输入appId' },
                            { id: 'v2', label: 'secretKey', type: 'password', required: true, rules: [this.requireRule('secretKey')], placeholder: '请输入secretKey' }
                        ]
                    },
                    {
                        id: 'SSH_PRIVATEKEY',
                        name: 'SSHKEY',
                        desc: 'SSH包含公钥和私钥,用于关联SVN类型代码库，SSH配置说明请参考蓝盾文档中心',
                        content: [
                            {
                                id: 'v1',
                                label: 'ssh私钥',
                                type: 'textarea',
                                required: true,
                                rules: [
                                    {
                                        validator: (val) => (/^(-----BEGIN RSA PRIVATE KEY-----){1}[\s\S]*(-----END RSA PRIVATE KEY-----)$/.test(val)),
                                        message: '请输入SSH Key对应的私钥，以-----BEGIN RSA PRIVATE KEY-----开头，-----END RSA PRIVATE KEY-----结束',
                                        trigger: 'blur'
                                    },
                                    this.requireRule('ssh私钥')
                                ],
                                placeholder: '请输入SSH Key对应的私钥，以-----BEGIN RSA PRIVATE KEY-----开头，-----END RSA PRIVATE KEY-----结束'
                            },
                            { id: 'v2', label: '私钥密码', type: 'password', placeholder: '请输入私钥密码' }
                        ]
                    },
                    {
                        id: 'TOKEN_SSH_PRIVATEKEY',
                        name: 'sshKeyToken',
                        desc: '用于使用ssh方式关联Git类型代码库',
                        content: [
                            { id: 'v1', label: 'private token', type: 'password', required: true, rules: [this.requireRule('private token')], placeholder: '请输入token' },
                            {
                                id: 'v2',
                                label: 'ssh私钥',
                                type: 'textarea',
                                required: true,
                                rules: [
                                    {
                                        validator: (val) => (/^(-----BEGIN RSA PRIVATE KEY-----){1}[\s\S]*(-----END RSA PRIVATE KEY-----)$/.test(val)),
                                        message: '请输入SSH Key对应的私钥，以-----BEGIN RSA PRIVATE KEY-----开头，-----END RSA PRIVATE KEY-----结束',
                                        trigger: 'blur'
                                    },
                                    this.requireRule('ssh私钥')
                                ],
                                placeholder: '请输入SSH Key对应的私钥，以-----BEGIN RSA PRIVATE KEY-----开头，-----END RSA PRIVATE KEY-----结束'
                            },
                            { id: 'v3', label: '私钥密码', type: 'password', placeholder: '请输入私钥密码' }
                        ]
                    },
                    {
                        id: 'TOKEN_USERNAME_PASSWORD',
                        name: 'passwordToken',
                        desc: '用于使用http方式关联Git类型代码库',
                        content: [
                            { id: 'v1', label: 'private token', type: 'password', required: true, rules: [this.requireRule('private token')], placeholder: '请输入token' },
                            { id: 'v2', label: '用户名', type: 'text', required: true, rules: [this.requireRule('用户名')], placeholder: '请输入用户名' },
                            { id: 'v3', label: '密码', type: 'password', required: true, rules: [this.requireRule('密码')], placeholder: '请输入密码' }
                        ]
                    }
                ],
                formData: {
                    credentialType: 'PASSWORD',
                    credentialId: '',
                    credentialName: '',
                    credentialRemark: '',
                    v1: '',
                    v2: '',
                    v3: '',
                    v4: ''
                },
                idRule: {
                    validator: (val) => (/^[a-zA-Z0-9\_]{1,40}$/.test(val)),
                    message: '以英文字母、数字或下划线(_)组成，不超过40个字',
                    trigger: 'blur'
                },
                nameRule: {
                    validator: (val) => (/^[\u4e00-\u9fa5a-zA-Z0-9\-\.\_]{0,30}$/.test(val)),
                    message: '以汉字、英文字母、数字、连字符(-)、下划线(_)或英文句号组成，不超过30个字，仅用于展示',
                    trigger: 'blur'
                },
                isLoading: false
            }
        },

        computed: {
            computedTicket () {
                return this.ticketTypes.find((x) => (x.id === this.formData.credentialType))
            },
            isEdit () {
                return this.form.permissions
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
                const defaultForm = {
                    credentialType: 'PASSWORD',
                    credentialId: '',
                    credentialName: '',
                    credentialRemark: '',
                    v1: '',
                    v2: '',
                    v3: '',
                    v4: ''
                }
                this.formData = Object.assign(defaultForm, this.form)
            },

            requireRule (name) {
                return {
                    required: true,
                    message: name + '是必填项',
                    trigger: 'blur'
                }
            },

            submitData () {
                this.$refs.credentialForm.validate(() => {
                    let method = setting.createTicket
                    const params = ['linetest', this.formData]
                    if (this.isEdit) {
                        method = setting.modifyTicket
                        params.push(this.formData.credentialId)
                    }
                    this.isLoading = true
                    method(...params).then(() => {
                        const message = this.isEdit ? '编辑成功' : '新增成功'
                        this.$bkMessage({ theme: 'success', message })
                        this.$emit('success')
                    }).catch((err) => {
                        this.$bkMessage({ theme: 'error', message: err.message || err })
                    }).finally(() => {
                        this.isLoading = false
                    })
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.content || err })
                })
            },

            changeCredentialType (credentialType) {
                this.$refs.credentialForm.clearError()
                this.formData.v1 = ''
                this.formData.v2 = ''
                this.formData.v3 = ''
                this.formData.v4 = ''
                this.$router.replace({
                    params: {
                        credentialType
                    }
                })
            },

            hidden () {
                this.$emit('hidden')
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .credential-form {
        padding: 20px 30px;
        /deep/ button {
            margin: 8px 10px 0 0;
        }
    }
</style>

<template>
    <section class="g-accelerate-box task-param">
        <h3 class="create-title g-accelerate-deep-black-font">加速参数</h3>
        <bk-form class="g-accelerate-form-left" :label-width="120" :model="copyFormData.configParam" ref="paramForm">
            <bk-form-item :label="config.paramName"
                :property="config.paramKey"
                v-for="config in paramConfig"
                :key="config.paramKey"
                :required="config.required"
                :desc="config.tips"
                :rules="requireRule(config)"
                error-display-type="normal"
            >
                <component :is="'param-' + config.paramType"
                    v-bind="config"
                    :disabled="!isEdit"
                    :param-value="copyFormData.configParam"
                    v-if="config.displayed"
                    @value-change="valueChange"
                ></component>
            </bk-form-item>
        </bk-form>
        <bk-button v-if="isEdit && !onlyEdit" theme="primary" class="g-accelerate-bottom-button" @click="save">保存</bk-button>
        <bk-button v-if="isEdit && !onlyEdit" class="g-accelerate-bottom-button" @click="cancel">取消</bk-button>
        <span class="g-accelerate-edit-button" @click="isEdit = !isEdit" v-if="!onlyEdit && (paramConfig || []).length && !isEdit"><logo name="edit" size="16"></logo>编辑</span>
    </section>
</template>

<script>
    import { mapGetters } from 'vuex'
    import logo from '@/components/logo'
    import renderComponents from '../render'
    import { modifyConfigParam } from '@/api'

    export default {
        components: {
            logo,
            ...renderComponents
        },

        props: {
            onlyEdit: {
                type: Boolean,
                default: true
            },
            formData: {
                type: Object
            }
        },

        data () {
            return {
                isEdit: this.onlyEdit,
                copyFormData: {},
                isLoading: false
            }
        },

        computed: {
            ...mapGetters('accelerate', ['paramConfig'])
        },

        created () {
            this.copyFormData = JSON.parse(JSON.stringify(this.formData))
        },

        methods: {
            valueChange (key, value) {
                this.$set(this.copyFormData.configParam, key, value)
            },

            requireRule (config) {
                const rules = []
                if (config.required) {
                    rules.push({
                        required: true,
                        message: this.$t('accelerate.validateMessage', [config.paramName, '必填项']),
                        trigger: 'blur'
                    })
                }
                return rules
            },

            validate () {
                return new Promise((resolve, reject) => {
                    const configLength = (this.paramConfig || []).length
                    if (configLength > 0) this.$refs.paramForm.validate().then(resolve, (validator) => reject(validator))
                    else resolve()
                })
            },

            save () {
                this.validate().then(() => {
                    this.isLoading = true
                    modifyConfigParam(this.copyFormData).then(() => {
                        this.$bkMessage({ theme: 'success', message: '修改成功' })
                        this.$emit('update:formData', this.copyFormData)
                        this.isEdit = false
                    }).catch((err) => {
                        this.$bkMessage({ theme: 'error', message: err.message || err })
                    }).finally(() => {
                        this.isLoading = false
                    })
                }, (validator) => {
                    this.$bkMessage({ message: validator.content || validator, theme: 'error' })
                })
            },

            cancel () {
                this.copyFormData = JSON.parse(JSON.stringify(this.formData))
                this.isEdit = false
            }
        }
    }
</script>

<style lang="scss" scoped>
    .task-param {
        position: relative;
    }

    .g-accelerate-box {
        margin-bottom: 20px;
        padding: 26px 32px;
        .create-title {
            font-size: 14px;
            line-height: 22px;
            margin-bottom: 17px;
        }
    }

    .single-width {
        width: 3.5rem;
        margin-right: 10px;
    }

    .double-width {
        width: 7.1rem;
    }
</style>

<template>
    <section class="g-accelerate-box task-param">
        <h3 class="create-title g-accelerate-deep-black-font">加速参数</h3>
        <bk-form :label-width="120" :model="formData">
            <bk-form-item :label="config.paramName"
                :property="config.paramKey"
                v-for="config in paramConfig"
                :key="config.paramKey"
            >
                <component :is="'param-' + config.paramType"
                    v-bind="config"
                    :param-enum="config.paramEnum"
                    :param-key="config.paramKey"
                    :is-edit="isEdit"
                    :param-value.sync="copyFormData.configParam"
                ></component>
            </bk-form-item>
        </bk-form>
        <bk-button v-if="isEdit && !onlyEdit" theme="primary" class="g-accelerate-bottom-button" @click="save">保存</bk-button>
        <bk-button v-if="isEdit && !onlyEdit" class="g-accelerate-bottom-button" @click="cancel">取消</bk-button>
        <span class="g-accelerate-edit-button" @click="isEdit = !isEdit" v-if="!onlyEdit && paramConfig.length"><logo name="edit-small" size="20"></logo>编辑</span>
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
            save () {
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

<template>
    <section class="g-accelerate-box basic-info">
        <h3 class="create-title g-accelerate-deep-black-font">基本信息</h3>
        <bk-form :label-width="120" :model="copyFormData" v-bkloading="{ isLoading: isLoadingEngine }">
            <bk-form-item label="方案ID" property="openStatus">
                <template v-if="isEdit">
                    <bk-input v-model="copyFormData.planId" class="single-width" placeholder="系统自动生成，方案的唯一标识" disabled></bk-input>
                    <bk-checkbox v-model="copyFormData.openStatus">开启方案</bk-checkbox>
                </template>
                <span v-else class="g-accelerate-text-break">{{ copyFormData.planId }}</span>
            </bk-form-item>
            <bk-form-item label="方案名称" required property="planName">
                <template v-if="isEdit">
                    <bk-input v-model="copyFormData.planName" class="single-width"></bk-input>
                </template>
                <span v-else class="g-accelerate-text-break">{{ formData.planName }}</span>
            </bk-form-item>
            <bk-form-item label="加速模式" required property="name">
                <template v-if="isEdit && onlyEdit">
                    <span>根据你的加速场景选择适用的模式</span>
                    <ul class="accelerate-model-list">
                        <li v-for="item in engineList" :key="item" :class="['single-width', 'accelerate-model-item', 'g-accelerate-text-overflow', { choose: copyFormData.engineCode === item.engineCode }]" @click="toggleChoose(item)">
                            <p class="item-title g-accelerate-black-font">{{ item.engineCode }}</p>
                            <span class="item-desc g-accelerate-gray-font">{{ item.desc }}</span>
                            <logo name="check-1" :size="10" class="item-check"></logo>
                        </li>
                    </ul>
                </template>
                <span v-else class="accelerate-model-engine">{{ formData.engineCode }}</span>
            </bk-form-item>
            <bk-form-item label="方案说明" property="name">
                <template v-if="isEdit">
                    <bk-input v-model="copyFormData.desc" type="textarea" class="double-width" :maxlength="200"></bk-input>
                </template>
                <span v-else class="g-accelerate-text-break">{{ formData.desc }}</span>
            </bk-form-item>
        </bk-form>
        <bk-button v-if="isEdit && !onlyEdit" theme="primary" class="g-accelerate-bottom-button" @click="save" :loading="isLoading">保存</bk-button>
        <bk-button v-if="isEdit && !onlyEdit" class="g-accelerate-bottom-button" @click="cancle" :disabled="isLoading">取消</bk-button>
        <span class="g-accelerate-edit-button" @click="isEdit = true" v-if="!onlyEdit"><logo name="edit-small" size="20"></logo>编辑</span>
    </section>
</template>

<script>
    import { mapActions } from 'vuex'
    import { getEngineList, modifyTaskBasic } from '@/api'
    import logo from '@/components/logo'

    export default {
        components: {
            logo
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
                engineList: [],
                isLoading: false,
                isLoadingEngine: false
            }
        },

        created () {
            this.copyFormData = JSON.parse(JSON.stringify(this.formData))
            this.getEngineList()
        },

        methods: {
            ...mapActions('accelerate', ['setParamConfig']),

            save () {
                this.isLoading = true
                modifyTaskBasic(this.copyFormData).then(() => {
                    this.$bkMessage({ theme: 'success', message: '修改成功' })
                    this.$emit('update:formData', this.copyFormData)
                    this.isEdit = false
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                })
            },

            cancle () {
                this.copyFormData = JSON.parse(JSON.stringify(this.formData))
                this.isEdit = false
            },

            toggleChoose (item) {
                const enginCode = item.engineCode
                const formData = JSON.parse(JSON.stringify(this.formData))
                if (this.copyFormData.engineCode === enginCode) {
                    this.copyFormData.engineCode = ''
                    this.copyFormData.paramConfig = []
                    formData.paramConfig = []
                    this.setParamConfig([])
                } else {
                    this.copyFormData.engineCode = enginCode
                    this.copyFormData.paramConfig = item.paramConfig
                    formData.paramConfig = item.paramConfig
                    this.setParamConfig(item.paramConfig)
                }
                this.$emit('update:formData', formData)
            },

            getEngineList () {
                this.isLoadingEngine = true
                getEngineList().then((res = []) => {
                    this.engineList = res
                    const curEngine = res.find((item) => (this.copyFormData.engineCode && item.engineCode === this.copyFormData.engineCode))
                    if (curEngine) {
                        this.setParamConfig(curEngine.paramConfig)
                    }
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoadingEngine = false
                })
            }
        }
    }
</script>

<style lang="scss" scoped>
    @import '../../assets/scss/var.scss';

    .basic-info {
        position: relative;
    }

    .accelerate-model-engine {
        background: #e1ecff;
        border-radius: 2px;
        font-size: 12px;
        line-height: 20px;
        color: #3a84ff;
        padding: 0 9px;
        display: inline-block;
    }

    .accelerate-model-list {
        margin-top: -5px;
        &::after {
            content: '';
            display: table;
            clear: both;
        }
        .accelerate-model-item {
            position: relative;
            float: left;
            height: round(60px * $designToPx);
            border: 1px solid #C4C6CC;
            border-radius: 2px;
            margin: 10px 10px 0 0;
            padding: 0 11px;
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            justify-content: center;
            cursor: pointer;
            .item-check {
                display: none;
                position: absolute;
                right: 1px;
                top: 1px;
                color: #fff;
            }
            .item-title {
                line-height: 22px;
            }
            .item-desc {
                font-size: 12px;
                line-height: 20px;
            }
            &:hover {
                border-color: #3a84ff;
                box-shadow: 0 0 0 2px #e1ecff;
            }
            &.choose {
                border-color: #3a84ff;
                &:before {
                    content: '';
                    position: absolute;
                    right: -15px;
                    top: -15px;
                    width: 30px;
                    height: 30px;
                    background: #3a84ff;
                    transform: rotate(45deg);
                }
                .item-check {
                    display: block;
                }
            }
        }
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

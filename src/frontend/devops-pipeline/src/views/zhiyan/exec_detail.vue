<template>
    <PipelineExecDetail
        ref="detail"
        v-bind="$attrs"
        v-on="$listeners"
    />
</template>

<script>
    import Vue from 'vue'
    import { mapState } from 'vuex'
    import PipelineExecDetail from '../subpages/exec_detail.vue'

    const TRY_LIMIT = 50

    export default {
        name: 'ZyPipelineExecDetail',
        components: {
            PipelineExecDetail
        },
        inject: ['sendMessage'],
        props: {
            deployTypes: {
                type: Array,
                deploy: () => []
            },
            deployLabel: {
                type: String,
                default: '部署详情'
            },
            extraActions: {
                type: Object,
                default: () => {}
            }
        },
        data () {
            return {
                extraAction: null,
                tryCnt: 0,
                timer: null
            }
        },
        computed: {
            ...mapState('atom', [
                'execDetail',
                'editingElementPos',
                'isPropertyPanelVisible'
            ]),
            showLog () {
                const { editingElementPos, isPropertyPanelVisible, $route: { params } } = this
                return typeof editingElementPos?.elementIndex !== 'undefined' && params?.buildNo && isPropertyPanelVisible
            },
            currentElement () {
                try {
                    const {
                        editingElementPos: { stageIndex, containerIndex, elementIndex },
                        execDetail: { model: { stages } }
                    } = this

                    const element = stages[stageIndex].containers[containerIndex].elements[elementIndex]

                    return element
                } catch (error) {
                    return null
                }
            }
        },
        watch: {
            async showLog (visible) {
                await this.onPanelVisibleChange(visible)
            }
        },
        beforeDestroy () {
            this.deleteExtraAction()
        },
        methods: {
            async onPanelVisibleChange (visible) {
                if (!visible || !this.currentElement?.status) {
                    return this.deleteExtraAction()
                }

                await this.$nextTick()

                this.onElementLogShow(this.currentElement)
            },
            onElementLogShow (element) {
                const { atomCode } = element || {}

                if ((this.deployTypes || []).includes(atomCode)) {
                    this.tryCnt = 0
                    this.tryAddExtraAction(element)
                }
            },
            tryAddExtraAction (element) {
                this.timer && clearTimeout(this.timer)

                if (this.tryCnt >= TRY_LIMIT) {
                    return
                }

                this.tryCnt += 1
                this.timer = setTimeout(() => this.addExtraAction(element), 100)
            },
            addExtraAction (element) {
                const parent = this.$el.querySelector('.log-head .head-tab')

                if (!parent) {
                    return this.tryAddExtraAction(element)
                }

                const el = document.createElement('span')

                parent.appendChild(el)

                const instance = new Vue({
                    render: (h, props) => h('span', {
                        // class: 'zy-log-action',
                        on: {
                            click: () => this.showDeployDetail({
                                element_id: element.id
                            })
                        }
                    }, this.deployLabel)
                })

                instance.$mount(el)
                this.extraAction = instance
            },
            deleteExtraAction () {
                this.timer && clearTimeout(this.timer)

                if (!this.extraAction) {
                    return
                }

                this.extraAction?.$el.remove()
                this.extraAction.$destroy()
                this.extraAction = null
            },
            showDeployDetail (params) {
                this.sendMessage('showDeployDetail', params)
            }
        }
    }
</script>

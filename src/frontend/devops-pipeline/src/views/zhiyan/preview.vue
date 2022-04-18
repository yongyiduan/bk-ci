<template>
    <PipelinePreview
        ref="preview"
        v-bind="$attrs"
        v-on="$listeners"
    />
</template>

<script>
    import Vue from 'vue'
    import PipelinePreview from '../subpages/preview.vue'

    export default {
        name: 'ZyPipelinePreview',
        components: {
            PipelinePreview
        },
        inject: ['sendMessage'],
        props: {
            execParams: {
                type: Object,
                default: () => {}
            }
        },
        data () {
            return {
                paramsButton: null,
                unWatch: null
            }
        },
        watch: {
            execParams () {
                this.$refs.preview.paramValues = this.execParams || {}
            }
        },
        async mounted () {
            await this.$nextTick()

            this.$refs.preview.paramValues = this.execParams || {}
            this.unWatch = this.$refs.preview.$watch('curPipelineInfo', async (curPipelineInfo) => {
                if (this.paramsButton) {
                    return
                }

                await this.$nextTick()

                const paramList = this.$refs.preview.paramList || []
                const buildList = this.$refs.preview.buildList || []

                if (paramList?.length) {
                    this.addParamsButton(!!buildList?.length)
                    this.sendMessage('editParams', {})
                }
            })
        },
        beforeDestroy () {
            this.paramsButton && this.paramsButton.$destroy()
            this.paramsButton = null

            this.unWatch && this.unWatch()
            this.unWatch = null
        },
        methods: {
            addParamsButton (hasBuildList) {
                if (this.paramsButton) {
                    return
                }

                const parent = this.$el.querySelector(hasBuildList ? '.global-params:nth-child(2) .item-title' : '.global-params .item-title')
                const el = document.createElement('div')

                if (!parent) {
                    return
                }

                el.className = 'zy-params-button'
                parent.appendChild(el)

                const instance = new Vue({
                    render: (h, props) => h('div', {
                        class: 'zy-params-button'
                    }, [
                        h('a', {
                            on: {
                                click: () => this.sendMessage('editParams', this.$refs.preview.paramValues)
                            }

                        }, '设置')
                    ])
                })

                instance.$mount(el)

                this.paramsButton = instance
            }
        }
    }
</script>

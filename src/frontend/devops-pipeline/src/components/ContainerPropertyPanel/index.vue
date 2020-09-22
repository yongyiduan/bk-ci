<template>
    <bk-sideslider class="bkci-property-panel" width="640" :is-show.sync="visible" :quick-close="true">
        <header class="container-panel-header" slot="header">
            {{ title }}
            <div v-if="showDebugDockerBtn" :class="!editable ? 'control-bar' : 'debug-btn'">
                <bk-button theme="warning" @click="startDebug">{{ $t('editPage.docker.debugConsole') }}</bk-button>
            </div>
        </header>
        <container-content v-bind="$props" slot="content" ref="container"></container-content>
    </bk-sideslider>
</template>

<script>
    import { mapActions, mapState, mapGetters } from 'vuex'
    import ContainerContent from './ContainerContent'

    export default {
        name: 'container-property-panel',
        components: {
            ContainerContent
        },
        props: {
            containerIndex: Number,
            stageIndex: Number,
            stages: Array,
            editable: Boolean,
            title: String
        },
        computed: {
            ...mapState('atom', [
                'execDetail',
                'isPropertyPanelVisible'
            ]),

            ...mapGetters('atom', [
                'getContainer',
                'getContainers',
                'getStage',
                'isDockerBuildResource',
                'checkShowDebugDockerBtn'
            ]),

            visible: {
                get () {
                    return this.isPropertyPanelVisible
                },
                set (value) {
                    this.togglePropertyPanel({
                        isShow: value
                    })
                }
            },

            stage () {
                const { stageIndex, stages } = this
                return this.getStage(stages, stageIndex)
            },

            containers () {
                const { stage, getContainers } = this
                return getContainers(stage)
            },

            container () {
                const { containers, containerIndex } = this
                return this.getContainer(containers, containerIndex)
            },

            showDebugDockerBtn () {
                return this.checkShowDebugDockerBtn(this.container, this.$route.name, this.execDetail)
            }
        },

        methods: {
            ...mapActions('atom', [
                'togglePropertyPanel'
            ]),

            startDebug () {
                this.$refs.container.startDebug()
            }
        }
    }
</script>

<style lang="scss">
    @import '../AtomPropertyPanel/propertyPanel';
    .container-panel-header {
        display: flex;
        margin-right: 20px;
        justify-content: space-between;
    }
</style>

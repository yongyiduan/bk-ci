<template>
    <bk-dialog
        v-model="show"
        theme="primary"
        :mask-close="false"
        header-position="left"
        v-bind="options"
    >
        <iframe v-if="show" @load="onload" ref="extensionIframe" class="extention-dialog-content-iframe" :src="src" />
    </bk-dialog>
</template>

<script lang='ts'>
    import Vue from 'vue'
    import eventBus from '@/utils/eventBus'
    import { Component } from 'vue-property-decorator'
    @Component
    export default class ExtensionDialog extends Vue {
        src: string = ''
        show: boolean = false
        options: object | null = null
        header: string = 'Title'
        customData: object | null = null
        loaded: boolean = false

        updateProps (props) {
            Object.keys(props).map(prop => {
              this[prop] = props[prop]
            })
        }

        onload () {
            try {
                // @ts-ignore
                this.$refs.extensionIframe.contentWindow.postMessage({
                    action: 'syncCustomData',
                    params: JSON.stringify(this.customData)
                }, '*')
            } catch (e) {
                console.warn('can not find extensionIframe')
            }
        }

        mounted () {
            eventBus.$on('update-extension-dialog', this.updateProps)
        }
        
        beforeDestroy () {
            eventBus.$off('update-extension-dialog', this.updateProps)
        }
    }
</script>

<style lang="scss">
    .extention-dialog-content-iframe {
        display: flex;
        width: 100%;
        overflow: auto;
        border: 0;
    }
</style>

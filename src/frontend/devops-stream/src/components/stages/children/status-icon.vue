<template>
    <i :class="[pluginStatusIcon, 'plugin-icon', getPipelineStatusClass(statusContainer.status)]" v-if="iconType === 'icon'"></i>
    <icon v-else name="build-hooks" size="18" :class="['svg-icon', getPipelineStatusClass(statusContainer.status)]"></icon>
</template>

<script>
    import { getPipelineStatusShapeIconCls, getPipelineStatusClass } from '@/components/status'

    export default {
        props: {
            statusContainer: Object
        },

        computed: {
            pluginStatusIcon () {
                return getPipelineStatusShapeIconCls(this.statusContainer.status || 'WAITING')
            },

            iconType () {
                let type = 'icon'
                const additionalOptions = this.statusContainer.additionalOptions || {}
                const elementPostInfo = additionalOptions.elementPostInfo
                if (elementPostInfo) {
                    type = 'hooks'
                }
                return type
            }
        },

        methods: {
            getPipelineStatusClass
        }
    }
</script>

<style lang="postcss" scoped>
    @import '@/css/conf';

    .plugin-icon {
        width: 42px;
        height: 42px;
        line-height: 42px;
    }
    .svg-icon {
        width: 42px;
    }
    .canceled, .waiting {
        color: $warningColor;
    }
    .pause {
        color: $pauseColor;
    }
    .warning {
        color: $warningColor;
    }
    .danger {
        color: $dangerColor;
    }
    .success {
        color: $successColor;
    }
</style>

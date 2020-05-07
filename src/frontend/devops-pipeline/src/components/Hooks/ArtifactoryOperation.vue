<template>
    <bk-dropdown-menu v-if="hasExts" align="right">
        <div slot="dropdown-trigger" class="hook-dropdown-trigger">
            <i class="entry-circle" v-for="i in [1, 2, 3]" :key="i" />
        </div>
        <ul class="artifactory-operation-hooks" slot="dropdown-content">
            <li v-for="ext in extensions" :key="ext.serviceId" @click="hookAction(ext)" :title="getExtTooltip(ext)">
                <img :src="getResUrl(getExtIconUrl(ext), ext.baseUrl)" />
                <span> {{ ext.serviceName }} </span>
            </li>
        </ul>
    </bk-dropdown-menu>
</template>

<script>
    import customExtMixin from '@/mixins/custom-extension-mixin'

    export default {
        name: 'artifactory-operation-hooks',
        mixins: [customExtMixin],
        props: {
            artifact: {
                type: Object,
                default: () => ({})
            }
        },
        computed: {
            hooks () {
                return this.artifactHooks
            }
        },
        methods: {
            hookAction (ext) {
                const { props = {} } = ext
                const { entryResUrl = 'index.html', options = {}, data = {} } = props
                this.$hookTrigger({
                    ...ext,
                    url: this.getResUrl(entryResUrl, ext.baseUrl),
                    name: ext.serviceName,
                    target: {
                        type: ext.htmlComponentType,
                        options,
                        data: {
                            ...data,
                            ...this.$route.params,
                            artifact: this.artifact
                        }
                    }
                })
            }
        }
    }
</script>

<style lang='scss'>
    @import '../../scss/mixins/ellipsis';

    .hook-dropdown-trigger {
        display: flex;
        width: 25px;
        height: 100%;
        flex-direction: row;
        justify-content: space-between;
        align-items: center;
        cursor: pointer;

        &:hover,
        &.active {
            i.entry-circle {
                background-color: #3c96ff;
            }
        }

        i.entry-circle {
            display: flex;
            width: 18px;
            background-color: #AEB1C0;
            width: 5px;
            height: 5px;
            border-radius: 50%;
            z-index: 1;
        }
    }
    .artifactory-operation-hooks {
        align-items: center;
        max-height: 188px;
        overflow: auto;
        > li {
            height: 32px;
            vertical-align: middle;
            padding: 0 16px;
            @include ellipsis();
            display: flex;
            align-items: center;
            span {
                color: #222222;
            }
            &:hover span {
                color: #3c96ff;
            }
            img {
                display: inline-block;
                margin: 0 6px 0 0;
                width: 16px;
                height: 16px;
            }

        }
    }
</style>

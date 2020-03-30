<template>
    <bk-dropdown-menu align="center">
        <i slot="dropdown-trigger" class="bk-icon icon-logo-store"></i>
        <ul class="artifactory-operation-hooks" slot="dropdown-content">
            <li v-for="ext in artifactExtensions" :key="ext.serviceId" @click="hookAction(ext)" :title="getExtTooltip(ext)">
                <img :src="getResUrl(getExtIconUrl(ext), ext.baseUrl)" />
                <span> {{ ext.serviceName }} </span>
            </li>
        </ul>
    </bk-dropdown-menu>
</template>

<script>
    import { mapGetters, mapActions } from 'vuex'
    import { isAbsoluteURL, urlJoin } from '@/utils/util'

    export default {
        name: 'artifactory-operation-hooks',
        props: {
            artifact: {
                type: Object,
                default: () => ({})
            }
        },
        data () {
            return {
                artifactExtensions: []
            }
        },
        computed: {
            ...mapGetters([
                'artifactHooks',
                'hookKeyMap'
            ]),
            hookIds () {
                return this.artifactHooks.map(hook => hook.itemId).join(',')
            }
        },
        created () {
            console.log(this.artifactHooks)
            this.fetchExt()
        },
        methods: {
            ...mapActions([
                'fetchExtensionByHookId'
            ]),
            getExtTooltip (ext) {
                return ext.props && ext.props.tooltip ? ext.props.tooltip : ext.tooltip
            },
            getExtIconUrl (ext) {
                return ext.props && ext.props.iconUrl ? ext.props.iconUrl : ext.iconUrl
            },
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
            },
            async fetchExt () {
                try {
                    const res = await this.fetchExtensionByHookId({
                        projectCode: this.$route.params.projectId,
                        itemIds: this.hookIds
                    })
                    console.log(res, this.hookIds, this.artifactHooks)
                    let artifactExtensions = []
                    this.artifactExtensionMap = res.data.reduce((artifactExtensionMap, ext) => {
                        const extServiceList = ext.extServiceList.map(item => ({
                            ...this.hookKeyMap[ext.itemId],
                            ...item
                        }))
                        console.log(extServiceList, this.hookKeyMap[ext.itemId])
                        artifactExtensionMap[ext.itemId] = [
                            ...extServiceList
                        ]

                        artifactExtensions = [
                            ...artifactExtensions,
                            ...extServiceList
                        ]

                        return artifactExtensionMap
                    }, {})
                    console.log(artifactExtensions)
                    this.artifactExtensions = artifactExtensions
                } catch (error) {
                    console.log(error)
                }
            },
            getResUrl (url, baseURL) {
                return isAbsoluteURL(url) ? url : urlJoin(baseURL, 'static', url)
            }
        }
    }
</script>

<style lang='scss'>
    @import '../../scss/mixins/ellipsis';
    .artifactory-operation-hooks {
        align-items: center;
        > li {
            line-height: 32px;
            vertical-align: middle;
            padding: 0 16px;
            @include ellipsis();
            img {
                display: inline-block;
                width: 16px;
                height: 16px;
            }
            
        }
    }
</style>

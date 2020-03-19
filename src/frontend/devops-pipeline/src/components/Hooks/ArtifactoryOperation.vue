<template>
    <ul class="artifactory-operation-hooks">
        <li class="icon-hook" v-for="ext in artifactExtensions" :key="ext.serviceId" @click="hookAction(ext)">
            <template v-if="ext.tooltip">
                <img v-bk-tooltips="ext.tooltip" :src="getResUrl(ext.iconUrl, ext.baseUrl)" />
            </template>
            <template v-else>
                <img :src="getResUrl(ext.iconUrl, ext.baseUrl)" />
            </template>
        </li>
    </ul>
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
            hookAction (ext) {
                this.$hookTrigger({
                    ...ext,
                    url: urlJoin(ext.baseUrl, 'static/index.html'),
                    name: ext.itemName,
                    target: {
                        type: ext.htmlComponentType,
                        options: {},
                        data: {
                            ...(ext.props.data ? ext.props.data : {}),
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
                console.log(url, baseURL, 'geturl')
                return isAbsoluteURL(url) ? url : urlJoin(baseURL, 'static', url)
            }
        }
    }
</script>

<style lang='scss'>
    .artifactory-operation-hooks {
        display: inline-flex;
        align-items: center;
        > li {
            margin-right: 8px;
            width: 16px;
            height: 16px;
            
        }
    }
</style>

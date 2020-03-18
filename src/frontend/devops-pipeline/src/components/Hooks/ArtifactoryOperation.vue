<template>
    <ul class="artifactory-operation-hooks">
        <li v-for="hook in artifactExtensions" :key="hook.key" @click="hookAction(hook)">
            <template v-if="hook.tooltip && hook.tooltip.value">
                <img v-bk-tooltips="hook.tooltip.value" v-bind="hook.icon" :src="getResUrl(hook.icon.url, hook.appKey)" />
            </template>
            <template v-else>
                <img v-bind="hook.icon" :src="getResUrl(hook.icon.url, hook.appKey)" />
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
                'artifactHooks'
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
            hookAction (hook) {
                console.log(hook)
                this.$hookTrigger({
                    ...hook,
                    target: {
                        ...hook.target,
                        data: {
                            ...hook.target.data,
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
                    console.log(res, this.hookIds)
                    // const mock = true
                    // if (mock) throw Error('mock')
                    let artifactExtensions = []
                    this.artifactExtensionMap = res.data.reduce((artifactExtensionMap, ext) => {
                        artifactExtensionMap[ext.itemId] = [
                            ...ext.extServiceList
                        ]

                        artifactExtensions = [
                            ...artifactExtensions,
                            ...ext.extServiceList
                        ]

                        return artifactExtensionMap
                    }, {})
                    this.artifactExtensions = artifactExtensions
                } catch (error) {
                    console.log(error)
                    this.artifactExtensions = [{
                        'location': 'system.top.navigation.bar',
                        'weight': 200,
                        'styleClasses': [
                            'webitem',
                            'system-present-webitem'
                        ],
                        'url': 'http://localhost:8081',
                        'context': 'addon',
                        'target': {
                            'type': 'asidePanel',
                            'options': {
                                'width': '800'
                            }
                        },
                        'tooltip': {
                            'value': 'Example tooltip'
                        },
                        'icon': {
                            'width': 16,
                            'height': 16,
                            'url': 'http://localhost:8081/icon.svg'
                        },
                        'name': {
                            'value': '1 WebItem'
                        },
                        'key': 'web-item-example'
                    }]
                    this.artifactExtensionMap = {
                        5: [{
                            'location': 'system.top.navigation.bar',
                            'weight': 200,
                            'styleClasses': [
                                'webitem',
                                'system-present-webitem'
                            ],
                            'url': '/helloworld',
                            'context': 'addon',
                            'target': {
                                'type': 'dialog',
                                'options': {
                                    'height': '100px',
                                    'width': '200px'
                                }
                            },
                            'tooltip': {
                                'value': 'Example tooltip'
                            },
                            'icon': {
                                'width': 16,
                                'height': 16,
                                'url': '/icon.svg'
                            },
                            'name': {
                                'value': 'Demo WebItem'
                            },
                            'key': 'web-item-example'
                        }]
                    }
                }
            },
            getResUrl (url, appKey) {
                console.log(url, appKey, 'geturl')
                return isAbsoluteURL(url) ? url : urlJoin(this.extensionKeyMap[appKey].baseURL, url)
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
        }
    }
</style>

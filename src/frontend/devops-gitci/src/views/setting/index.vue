<template>
    <article class="setting-home">
        <aside class="aside-nav">
            <h3 class="nav-title">
                <i class="bk-icon icon-arrows-left" @click="backHome"></i>
                Settings
            </h3>
            <ul>
                <li v-for="setting in settingList"
                    :key="setting.name"
                    @click="goToPage(setting)"
                    :class="{ 'nav-item text-ellipsis': true, active: curSetting.name === setting.name }"
                >
                    <icon :name="setting.icon" size="18"></icon>
                    <span class="text-ellipsis item-text" v-bk-overflow-tips>{{ setting.label }}</span>
                </li>
            </ul>
        </aside>

        <router-view class="setting-main"></router-view>
    </article>
</template>

<script>
    import { modifyHtmlTitle } from '@/utils'

    export default {
        data () {
            return {
                settingList: [
                    { label: 'Basic Settings', name: 'basicSetting', icon: 'edit' },
                    { label: 'Credential Settings', name: 'credentialList', icon: 'lock' },
                    { label: 'Agent Pools', name: 'agentPools', icon: 'cc-cabinet' }
                ],
                curSetting: {}
            }
        },

        watch: {
            '$route.name': {
                handler (name) {
                    this.initRoute(name)
                },
                immediate: true
            }
        },

        created () {
            this.setHtmlTitle()
        },

        methods: {
            initRoute (name) {
                let settingIndex = 0
                switch (name) {
                    case 'basicSetting':
                        settingIndex = 0
                        break
                    case 'credentialList':
                        settingIndex = 1
                        break
                    case 'agentPools':
                    case 'agentList':
                    case 'addAgent':
                    case 'agentDetail':
                        settingIndex = 2
                        break
                }
                this.curSetting = this.settingList[settingIndex]
            },

            setHtmlTitle () {
                const projectPath = decodeURIComponent((this.$route.hash || '').slice(1))
                const title = projectPath + ' : Setting'
                modifyHtmlTitle(title)
            },

            goToPage ({ name, params }) {
                this.$router.push({ name, params })
            },

            backHome () {
                this.goToPage({ name: 'buildList' })
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .setting-home {
        display: flex;
        flex-direction: row;
        .setting-main {
            flex: 1;
        }
    }
</style>

<template>
    <article class="setting-home">
        <aside class="aside-nav">
            <h3 class="nav-title">
                <i class="bk-icon icon-arrows-left" @click="backHome"></i>
                Setting
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
                    case 'addAgent':
                        settingIndex = 2
                        break
                }
                this.curSetting = this.settingList[settingIndex]
            },

            goToPage ({ name, params }) {
                this.$router.push({ name, params })
            },

            backHome () {
                this.goToPage({ name: 'buildList', params: { pipelineId: 'all' } })
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
            background: #f5f6fa;
        }
    }
</style>

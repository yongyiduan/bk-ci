<template>
    <article class="notifications-home">
        <aside class="aside-nav">
            <h3 class="nav-title">
                <i class="bk-icon icon-arrows-left" @click="backHome"></i>
                Notifications
            </h3>
            <ul>
                <li v-for="nav in navList"
                    :key="nav.name"
                    :class="{ 'nav-item': true, active: curNav.name === nav.name }"
                ><icon :name="nav.icon" size="18"></icon>{{ nav.label }}</li>
            </ul>
        </aside>

        <main class="notifications-main">
            <section class="notifications-head">
                <bk-button class="notifications-button" @click="readAll">Mark all as read</bk-button>
                <span class="head-text">是否只展示已读：</span>
                <bk-switcher v-model="onlyUnread"></bk-switcher>
            </section>
            <bk-collapse v-bkloading="{ isLoading }">
                <bk-collapse-item :name="index" v-for="(notification, index) in notificationList" :key="index">
                    {{ notification.time }}
                    <bk-collapse slot="content">
                        <bk-collapse-item :name="request.messageTitle" v-for="request in notification.records" :key="request.messageTitle">
                            <span :class="{ 'message-status': true, 'unread': request.haveRead }"></span>{{ request.messageTitle }} （{{ request.contentAttr.failedNum }} / {{ request.contentAttr.total }}）
                            <bk-table :data="request.content.buildRecords" slot="content" class="f13">
                                <bk-table-column>
                                    <template slot-scope="props">
                                        {{ props.row.displayName || '--' }}
                                    </template>
                                </bk-table-column>
                                <bk-table-column>
                                    <template slot-scope="props">
                                        {{ (props.row.buildHistory || {}).buildNum || '--' }}
                                    </template>
                                </bk-table-column>
                                <bk-table-column prop="reason"></bk-table-column>
                                <bk-table-column prop="reasonDetail"></bk-table-column>
                            </bk-table>
                        </bk-collapse-item>
                    </bk-collapse>
                </bk-collapse-item>
            </bk-collapse>

            <bk-exception class="exception-wrap-item exception-part" type="empty" v-if="notificationList.length <= 0"></bk-exception>

            <bk-pagination small
                :current.sync="compactPaging.current"
                :count.sync="compactPaging.count"
                :limit="compactPaging.limit"
                :show-limit="false"
                @change="pageChange"
                class="notify-paging"
            />
        </main>
    </article>
</template>

<script>
    import { notifications } from '@/http'

    export default {
        data () {
            return {
                navList: [
                    { label: 'Inbox', name: 'inbox', icon: 'notify' }
                ],
                compactPaging: {
                    limit: 10,
                    current: 1,
                    count: 0
                },
                curNav: { name: 'inbox' },
                notificationList: [],
                onlyUnread: false,
                isLoading: false
            }
        },

        watch: {
            onlyUnread () {
                this.initData()
            }
        },

        created () {
            this.initData()
        },

        methods: {
            initData () {
                this.isLoading = true
                const params = {
                    messageType: 'REQUEST',
                    page: this.compactPaging.current,
                    pageSize: this.compactPaging.limit
                }
                if (this.onlyUnread) {
                    params.haveRead = false
                }
                notifications.getUserMessages(params).then((res) => {
                    this.notificationList = res.records
                    this.compactPaging.count = res.count
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                })
            },

            readAll () {
                notifications.readAllMessages().then(() => {
                    this.initData()
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                })
            },

            pageChange (page) {
                this.compactPaging.current = page
                this.initData()
            },

            goToPage ({ name }) {
                this.$router.push({ name })
            },

            backHome () {
                this.goToPage({ name: 'buildList', params: this.$route.params })
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .notifications-home {
        display: flex;
        flex-direction: row;
        background: #f5f6fa;
        .notifications-main {
            flex: 1;
            background: #fff;
            margin: 25px;
            padding: 25px;
            over-flow: auto;
            .notifications-head {
                display: flex;
                align-items: center;
                margin-bottom: 20px;
                .head-text {
                    display: inline-block;
                    margin: 0 3px 0 40px;
                }
            }
            .message-status {
                display: inline-block;
                margin: 5px 8px 0 0;
                height: 15px;
                width: 15px;
                border-radius: 100px;
                background: #f0f1f5;
                &.unread {
                    background: #ff5656;
                }
            }
        }
    }
    .notify-paging {
        margin: 12px 0 0;
        display: flex;
        align-items: center;
        justify-content: center;
        /deep/ span {
            outline: none;
            margin-left: 0;
        }
    }
</style>

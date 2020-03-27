<template>
    <section class="media-List" ref="mediaList">
        <ul class="detail-swiper" :style="{ width: maxTransferWidth + 'px', left: `${swiperTransfer}px` }">
            <li v-for="media in list" :key="media.id" class="media-item">
                <span v-if="media.mediaType === 'PICTURE'"
                    class="media-image"
                    :style="`background-image: url(${media.mediaUrl})`"
                    @click="fullScreenImgSrc = media.mediaUrl"
                >
                </span>
                <video v-else
                    controls="true"
                    preload="auto"
                    webkit-playsinline="true"
                    playsinline="true"
                    x-webkit-airplay="allow"
                    x5-video-player-type="h5"
                    x5-video-player-fullscreen="true"
                    x5-video-orientation="portraint"
                    style="object-fit: fill; width: 100%; height: 100%"
                    :src="media.mediaUrl">
                </video>
            </li>
        </ul>
        <i class="swiper-nav nav-left" @click="changeIndex(-1)" v-if="swiperTransfer > -maxTransferWidth + containWidth"></i>
        <i class="swiper-nav nav-right" @click="changeIndex(1)" v-if="swiperTransfer < 0"></i>

        <transition name="fade">
            <section class="full-screen" v-if="fullScreenImgSrc" @click="fullScreenImgSrc = ''">
                <img :src="fullScreenImgSrc">
            </section>
        </transition>
    </section>
</template>

<script>
    export default {
        props: {
            list: {
                type: Array
            }
        },

        data () {
            return {
                swiperTransfer: 0,
                fullScreenImgSrc: '',
                containWidth: 0
            }
        },

        computed: {
            maxTransferWidth () {
                const mediaList = this.list || []
                return mediaList.length * 395 + (mediaList.length - 1) * 37
            }
        },

        mounted () {
            const mediaList = this.$refs.mediaList
            this.containWidth = mediaList.offsetWidth
        },

        methods: {
            getPlayOption (media) {
                return {
                    playbackRates: [0.5, 1.0, 1.5, 2.0], // 可选的播放速度
                    autoplay: false, // 如果为true,浏览器准备好时开始回放。
                    muted: false, // 默认情况下将会消除任何音频。
                    loop: false, // 是否视频一结束就重新开始。
                    preload: 'auto', // 建议浏览器在<video>加载元素后是否应该开始下载视频数据。auto浏览器选择最佳行为,立即开始加载视频（如果浏览器支持）
                    aspectRatio: '16:9', // 将播放器置于流畅模式，并在计算播放器的动态大小时使用该值。值应该代表一个比例 - 用冒号分隔的两个数字（例如"16:9"或"4:3"）
                    fluid: false, // 当true时，Video.js player将拥有流体大小。换句话说，它将按比例缩放以适应其容器。
                    sources: [{
                        type: 'video/mp4', // 类型
                        src: media.mediaUrl // url地址
                    }],
                    controlBar: {
                        timeDivider: true, // 当前时间和持续时间的分隔符
                        durationDisplay: true, // 显示持续时间
                        remainingTimeDisplay: false, // 是否显示剩余时间功能
                        fullscreenToggle: true // 是否显示全屏按钮
                    }
                }
            },

            changeIndex (num) {
                let newTransferDis = this.swiperTransfer + num * 432
                const maxMove = this.maxTransferWidth - this.containWidth
                if (newTransferDis >= 0) newTransferDis = 0
                if (newTransferDis <= -maxMove) newTransferDis = -maxMove
                this.swiperTransfer = newTransferDis
            }
        }
    }
</script>

<style lang="scss" scoped>
    @import '@/assets/scss/conf.scss';

    .media-List {
        width: 100%;
        height: 222px;
        position: relative;
        overflow: hidden;
    }

    .full-screen {
        position: fixed;
        top: 0;
        left: 0;
        bottom: 0;
        right: 0;
        z-index: 2;
        background: rgba(0, 0, 0, 0.6);
        img {
            position: relative;
            top: 50%;
            left: 50%;
            transform: translate3d(-50%, -50%, 0);
            max-height: 50vh;
            max-width: 50vw;
        }
    }

    .detail-swiper {
        position: absolute;
        transition: 0.525s cubic-bezier(0.42, 0, 0.58, 1);
        &::after {
            content: '';
            display: table;
            clear: both;
        }
        .media-item {
            float: left;
            height: 222px;
            width: 395px;
            margin-right: 37px;
            cursor: pointer;
            .media-image {
                display: inline-block;
                background-color: #F6F7FA;
                background-position: center;
                background-repeat: no-repeat;
                background-size: contain;
                height: 222px;
                width: 395px;
            }
            &:last-child {
                margin-right: 0;
            }
        }
    }

    .swiper-nav {
        cursor: pointer;
        position: absolute;
        display: block;
        width: 12px;
        height: 12px;
        border-left: 3px solid $navGray;
        border-bottom: 3px solid $navGray;
        opacity: .6;
        top: 105px;
        &.nav-left {
            left: 12px;
            transform: rotate(45deg);
            &:hover {
                transform: rotate(45deg) scale(1.1);
            }
        }
        &.nav-right {
            right: 12px;
            transform: rotate(225deg);
            &:hover {
                transform: rotate(225deg) scale(1.1);
            }
        }
        &:hover {
            opacity: .9;
        }
        &:active {
            opacity: .8;
        }
    }
</style>

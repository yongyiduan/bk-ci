<template>
    <main>
        <fail></fail>
        <section class="code-check-detail">
            <h3 class="detail-title score">
                评分<span>计算公式 <icon name="tiaozhuan" :size="10"></icon> </span>
            </h3>
            <ul class="score-list float-left">
                <li v-for="score in scoreList" :key="score" class="score-detail">
                    <p class="score-circle">
                        <span class="circle-main"></span>
                        <span class="sector-group">
                            <span class="circle-sector"
                                v-for="(item, index) in getColorList(score)"
                                :key="item.color"
                                :style="{
                                    color: item.color,
                                    transform: `${stratTransition ? `rotate(${item.deg}deg)` : ''}`,
                                    transition: `transform 5.555ms linear ${5.555 * index}ms`,
                                    zIndex: 36 - index
                                }">
                            </span>
                        </span>
                    </p>
                    <p class="score-rate">
                        <span class="score-num"><bk-animate-number :value="score"></bk-animate-number></span>
                        <span class="score-title">代码安全</span>
                    </p>
                </li>
            </ul>
        </section>
        <section class="code-check-detail">
            <h3 class="detail-title">问题汇总</h3>
            <ul class="float-left">
                <li v-for="problem in problemList" :key="problem" class="problem-item">
                    <p class="problem-desc">
                        <span class="english-name">Coverity</span>
                        <span class="problem-name">代码缺陷</span>
                    </p>
                    <p class="problem-num">
                        <span class="num">3</span>
                        <span class="unit">个</span>
                    </p>
                </li>
            </ul>
        </section>
    </main>
</template>

<script>
    import fail from './fail'

    export default {
        components: {
            fail
        },

        data () {
            return {
                scoreList: [60, 99, 100],
                problemList: [1, 2, 3, 4, 5, 6, 7],
                stratTransition: false
            }
        },

        mounted () {
            setTimeout(() => {
                this.stratTransition = true
            }, 20)
        },

        methods: {
            getColorList (score) {
                const colorMap = {
                    '^100$': {
                        start: { r: 66, g: 214, b: 179 },
                        end: { r: 171, g: 249, b: 176 }
                    },
                    '^[9][0-9]$': {
                        start: { r: 247, g: 107, b: 28 },
                        end: { r: 250, g: 217, b: 97 }
                    },
                    '^[^9][0-9]$': {
                        start: { r: 234, g: 54, b: 54 },
                        end: { r: 253, g: 156, b: 156 }
                    }
                }

                let curColorRange = {}
                Object.keys(colorMap).forEach((key) => {
                    const reg = new RegExp(key)
                    if (reg.test(score)) curColorRange = colorMap[key]
                })

                function getColor (curScore) {
                    const rate = curScore / score
                    const rgb = []
                    const { start, end } = curColorRange || {};
                    ['r', 'g', 'b'].forEach((key) => {
                        const colorNum = (end[key] - start[key]) * rate + start[key]
                        rgb.push(colorNum)
                    })
                    return `rgb(${rgb.join(', ')})`
                }

                let curScore = 0
                const colorList = []
                while (curScore < score) {
                    const color = getColor(curScore)
                    const dis = (curScore + 2.777) > score ? score - curScore : 2.777
                    curScore += dis
                    const deg = (curScore - 2.777) * 3.6
                    colorList.push({ color, deg })
                }
                return colorList
            }
        }
    }
</script>

<style lang="scss" scoped>
    .code-check-detail {
        padding: 40px 3.2vh 0;
        .detail-title {
            font-size: 14px;
            line-height: 20px;
            color: #313238;
        }
        .score {
            margin-bottom: 26px;
        }
    }
    .float-left {
        &::after {
            content: '';
            display: table;
            clear: both;
        }
        > * {
            float: left;
        }
    }
    .score-list {
        .score-detail {
            height: 60px;
            display: flex;
            margin-right: 60px;
            &:last-child {
                margin: 0;
            }
            .score-circle {
                position: relative;
                width: 60px;
                height: 60px;
                border-radius: 50%;
                overflow: hidden;
                background: #f0f1f5;
                margin-right: 24px;
                .circle-main {
                    position: absolute;
                    top: 20px;
                    left: 20px;
                    width: 20px;
                    height: 20px;
                    border-radius: 50%;
                    background: #fff;
                    z-index: 55;
                }
                .sector-group {
                    width: 100%;
                    height: 100%;
                    position: absolute;
                    left: 0;
                    top: 0;
                }
                .circle-sector {
                    width: 100%;
                    height: 100%;
                    position: absolute;
                    clip: rect(30px 60px 60px 0px);
                    overflow: hidden;
                }
                .circle-sector:after {
                    content: '';
                    width: 100%;
                    height: 100%;
                    background: currentColor;
                    position: absolute;
                    clip: rect(0 60px 30px 0);
                    transform: rotate(12deg);
                    border-radius: 50%;
                }
            }
            .score-rate {
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: center;
                width: 60px;
                .score-num {
                    line-height: 45px;
                    font-size: 32px;
                    color: #313238;
                }
                .score-title {
                    font-size: 12px;
                    color: #63656e;
                    line-height: 17px;
                }
            }
        }
    }
    .problem-item {
        box-sizing: border-box;
        width: 25%;
        height: 80px;
        border: 1px solid #dcdee5;
        border-right: none;
        padding: 19px 15px 18px 30px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        cursor: pointer;
        margin-top: 16px;
        .problem-desc {
            display: flex;
            align-items: center;
            flex-direction: column;
            .english-name {
                font-weight: medium;
                line-height: 22px;
                font-size: 16px;
                color: #63656e;
                margin-bottom: 4px;
            }
            .problem-name {
                font-size: 12px;
                line-height: 17px;
                color: #979ba5;
            }
        }
        .problem-num {
            .num {
                font-weight: medium;
                font-size: 32px;
                line-height: 45px;
                color: #313238;
            }
            .unit {
                font-size: 12px;
                line-height: 17px;
                color: #979ba5;
            }
        }
        &:last-child, &:nth-child(4n) {
            border-right: 1px solid #dcdee5;
        }
        &:hover {
            border: 1px solid #3a84ff;
            border-right: none;
            position: relative;
            &::after {
                content: '';
                position: absolute;
                right: -1px;
                top: -1px;
                background: #3a84ff;
                height: 80px;
                width: 1px;
            }
            &:last-child, &:nth-child(4n) {
                border-right: 1px solid #3a84ff;
            }
            .english-name, .num, .unit {
                color: #3a84ff;
            }
        }
    }
</style>

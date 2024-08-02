package io.github.toyota32k.sample

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.toyota32k.shared.gesture.Direction
import io.github.toyota32k.shared.gesture.IUtManipulationTarget
import io.github.toyota32k.shared.gesture.Orientation
import io.github.toyota32k.shared.gesture.UtGestureInterpreter
import io.github.toyota32k.shared.gesture.UtManipulationAgent
import io.github.toyota32k.shared.gesture.UtSimpleManipulationTarget
import io.github.toyota32k.utils.UtLog
import kotlinx.coroutines.launch
import java.util.EnumSet
import kotlin.math.max

class MainActivity : AppCompatActivity() {
    // ビューモデルｗ
    private var pageNo:Int = 1
    private var maxPageNo:Int = 5

    // UtGestureInterpreter を作成
    // - UtManipulationAgentで使うので、scaleイベントも扱う
    private val gestureInterpreter: UtGestureInterpreter by lazy { UtGestureInterpreter(applicationContext, enableScaleEvent = true, rapidTap = false) }
    // UtManipulationAgent を作成（UtSimpleManipulationTargetを使用）
    private val manipulationAgent: UtManipulationAgent by lazy { UtManipulationAgent(manipulationTarget/*IUtManipulationTargetインスタンス*/) }

    private val manipulationTarget: IUtManipulationTarget by lazy {
        UtSimpleManipulationTarget(findViewById<FrameLayout>(R.id.parent_view), findViewById<TextView>(R.id.content_view), 0.4f, 0f, EnumSet.of(Orientation.Horizontal))
            .callbacks {
                changePage { orientation, dir ->
                    // change page
                    when(dir) {
                        Direction.Start -> previousPage()
                        Direction.End -> nextPage()
                    }
                    true
                }
                hasNextPage { orientation, dir ->
                    when(dir) {
                        Direction.Start -> pageNo>1
                        Direction.End -> pageNo<maxPageNo
                    }
                }
            }
    }

    val containerView get() = findViewById<FrameLayout>(R.id.parent_view)
    val targetView get() = findViewById<TextView>(R.id.content_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gestureInterpreter.setup(this, containerView) {
            onTap {
                addPage()      // TAP to add a page.
            }
            onLongTap {
                removePage()    // LongTAP to remove a page.
            }
            onDoubleTap {
                manipulationAgent.resetScrollAndScale() // DoubleTAP to reset scroll position and scale.
            }
            // onScroll / onScale を manipulationAgentに接続
            onScroll(manipulationAgent::onScroll)
            onScale(manipulationAgent::onScale)
        }

        updatePage()
    }

    private fun updatePage() {
        targetView.text = "Page ${pageNo}/${maxPageNo}"
    }


    private val logger = UtLog("Sample")

    private fun addPage() {
        lifecycleScope.launch {
            logger.debug()
            maxPageNo++
            pageNo = maxPageNo
            updatePage()
        }
    }
    private fun removePage() {
        logger.debug()
        if(maxPageNo>1) {
            maxPageNo--
            pageNo = max(pageNo,maxPageNo)
            updatePage()
        }
    }
    private fun previousPage() {
        logger.debug()
        if(pageNo>1) {
            pageNo--
            updatePage()
        }
    }
    private fun nextPage() {
        logger.debug()
        if(pageNo<maxPageNo) {
            pageNo++
            updatePage()
        }
    }

}

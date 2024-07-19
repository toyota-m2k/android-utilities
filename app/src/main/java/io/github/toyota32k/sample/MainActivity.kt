package io.github.toyota32k.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import io.github.toyota32k.sample.R
import io.github.toyota32k.shared.gesture.Direction
import io.github.toyota32k.shared.gesture.IUtManipulationTarget
import io.github.toyota32k.shared.gesture.Orientation
import io.github.toyota32k.shared.gesture.UtGestureInterpreter
import io.github.toyota32k.shared.gesture.UtManipulationAgent
import io.github.toyota32k.shared.gesture.UtSimpleManipulationTarget
import io.github.toyota32k.utils.UtLog
import java.util.EnumSet

class MainActivity : AppCompatActivity() {
    // UtGestureInterpreter を作成
    // この例では、
    // - ダブルタップは処理しない（-->その分、シングルタップの判定が少し速い）
    // - UtManipulationAgentで使うので、scaleイベントも扱う
    private val gestureInterpreter: UtGestureInterpreter by lazy { UtGestureInterpreter(applicationContext, enableScaleEvent = true, rapidTap = true) }
    // UtManipulationAgent を作成
    // この例では、MainActivity自体がIUtManipulationTargetを実装しているので、thisを引数に渡して構築。
    private val manipulationAgent: UtManipulationAgent by lazy { UtManipulationAgent(manipulationTarget/*IUtManipulationTargetインスタンス*/) }

    private val manipulationTarget: IUtManipulationTarget by lazy {
        UtSimpleManipulationTarget(findViewById<FrameLayout>(R.id.parent_view), findViewById<TextView>(R.id.content_view), 0.4f, 0f, EnumSet.of(Orientation.Horizontal))
            .callbacks {
                changePage { orientation, dir ->
                    // change page
                    when(dir) {
                        Direction.Start -> nextPage()
                        Direction.End -> previousPage()
                    }
                    true
                }
                hasNextPage { orientation, dir ->
                    true
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val containerView = findViewById<FrameLayout>(R.id.parent_view)
        val targetView = findViewById<TextView>(R.id.content_view)
        gestureInterpreter.setup(this, containerView) {
            onTap {
                edit()      // タップイベントで、テキストの編集モードを開始する
            }
            onLongTap {
                showProperty()    //ロングタップで、プロパティ表示
            }
            onFlickHorizontal { e->
                when (e.direction) {
                    Direction.Start -> previousPage()
                    Direction.End -> nextPage()
                }
            }
            // onScroll / onScale を manipulationAgentに接続
            onScroll(manipulationAgent::onScroll)
            onScale(manipulationAgent::onScale)
        }
    }

    private val logger = UtLog("Sample")
    private fun edit() {
        logger.debug()
    }
    private fun showProperty() {
        logger.debug()
    }
    private fun previousPage() {
        logger.debug()
    }
    private fun nextPage() {
        logger.debug()
    }

}

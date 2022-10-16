package com.codeboy.mediafacer.tools

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

object Utils {

    //a combination of reusable utility methods

    fun calculateNoOfColumns(context: Context, columnWidthDp: Float): Int { // For example columnWidthdp=180
        val displayMetrics = context.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        //return (screenWidthDp / columnWidthDp + 0.5).toInt()
        return (screenWidthDp / columnWidthDp).toInt()
    }

    class MarginItemDecoration(private val spaceHeight: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View,
                                    parent: RecyclerView, state: RecyclerView.State) {
            with(outRect) {
                if (parent.getChildAdapterPosition(view) == 0) {
                    top = spaceHeight
                }
                left =  spaceHeight
                right = spaceHeight
                bottom = spaceHeight
            }
        }
    }


    //spacing is in px (so you can convert dp to px using Math.round(someDpValue * getResources().getDisplayMetrics().density))
    class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int, private val includeEdge: Boolean) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view) // item position
            val column = position % spanCount // item column
            if (includeEdge) {
                outRect.left =
                    spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
                outRect.right =
                    (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)
                if (position < spanCount) { // top edge
                    outRect.top = spacing
                }
                outRect.bottom = spacing // item bottom
            } else {
                outRect.left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
                outRect.right =
                    spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing // item top
                }
            }
        }
    }


    class MarginDecoration(private val size: Int, private val edgeEnabled: Boolean) : RecyclerView.ItemDecoration(){
        private val NOSPACING = 0

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            // Separate layout type
            when (val layoutManager = parent.layoutManager) {
                is GridLayoutManager -> {
                    makeGridSpacing(
                        outRect,
                        parent.getChildAdapterPosition(view),
                        state.itemCount,
                        layoutManager.orientation,
                        layoutManager.spanCount,
                        layoutManager.reverseLayout
                    )
                }
            }
        }

        private fun makeGridSpacing(
            outRect: Rect,
            position: Int,
            itemCount: Int,
            @RecyclerView.Orientation orientation: Int,
            spanCount: Int,
            isReversed: Boolean
        ) {
            // Basic item positioning
            val isLastPosition = position == (itemCount - 1)
            val sizeBasedOnEdge = if (edgeEnabled) size else NOSPACING
            val sizeBasedOnLastPosition = if (isLastPosition) sizeBasedOnEdge else size

            // Opposite of spanCount (find the list depth)
            val subsideCount = if (itemCount % spanCount == 0) {
                itemCount / spanCount
            } else {
                (itemCount / spanCount) + 1
            }

            // Grid position. Imagine all items ordered in x/y axis
            val xAxis = position % spanCount
            val yAxis = position / spanCount

            // Conditions in row and column
            val isFirstColumn = xAxis == 0
            val isFirstRow = yAxis == 0
            val isLastColumn = xAxis == spanCount - 1
            val isLastRow = yAxis == subsideCount - 1

            // Saved size
            val sizeBasedOnFirstColumn = if (isFirstColumn) sizeBasedOnEdge else NOSPACING
            val sizeBasedOnLastColumn = if (!isLastColumn) sizeBasedOnLastPosition else sizeBasedOnEdge
            val sizeBasedOnFirstRow = if (isFirstRow) sizeBasedOnEdge else NOSPACING
            val sizeBasedOnLastRow = if (!isLastRow) size else sizeBasedOnEdge

            when (orientation) {
                RecyclerView.VERTICAL -> { // Column fixed. Number of columns is spanCount
                    with(outRect) {
                        left = sizeBasedOnFirstColumn
                        top = sizeBasedOnFirstRow
                        right = sizeBasedOnLastColumn
                        bottom = sizeBasedOnLastRow
                    }
                }
            }
        }


    }








}
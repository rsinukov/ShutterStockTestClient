package com.rsinukov.shutterstockclient

import android.support.v4.app.Fragment

open class BaseFragment : Fragment() {

    override fun onDestroy() {
        if (activity!!.isFinishing) {
            onScopeFinished()
            super.onDestroy()
            return
        }

        if (isStateSaved) {
            super.onDestroy()
            return
        }

        var anyParentIsRemoving = false
        var parent = parentFragment
        while (!anyParentIsRemoving && parent != null) {
            anyParentIsRemoving = parent.isRemoving
            parent = parent.parentFragment
        }

        if (isRemoving || anyParentIsRemoving) {
            onScopeFinished()
        }

        super.onDestroy()
    }

    protected open fun onScopeFinished() {
    }
}

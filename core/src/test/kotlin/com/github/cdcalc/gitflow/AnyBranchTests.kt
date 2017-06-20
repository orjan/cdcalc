package com.github.cdcalc.gitflow

import com.github.cdcalc.*
import com.github.cdcalc.data.SemVer
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class AnyBranchTests {
    lateinit var sut: Calculate
    lateinit var git: Git

    @Before
    fun before() {
        git = initGitFlow()
        sut = Calculate(git)
    }

    @Test
    fun should_resolve_master_branch() {
        git.createTaggedCommit("v2.0.0-rc.0")
        git.checkout("foobar", true)

        val result: GitFacts = sut.gitFacts()

        val head = git.repository.resolve(Constants.HEAD)

        assertEquals(SemVer(2,1,0, listOf("alpha", head.abbreviate(7).name().toUpperCase())), result.semVer)
    }

}
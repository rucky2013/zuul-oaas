/*
 * The MIT License
 *
 * Copyright 2013-2016 Czech Technical University in Prague.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package cz.cvut.zuul.oaas.repos

import cz.cvut.zuul.oaas.models.PersistableApproval
import org.springframework.beans.factory.annotation.Autowired

abstract class ApprovalsRepoIT extends BaseRepositoryIT<PersistableApproval> {

    @Autowired ApprovalsRepo repo


    PersistableApproval modifyEntity(PersistableApproval entity) {
        entity.approved = !entity.approved
        entity
    }


    def 'find one approval by userId, clientId and scope'() {
        setup:
            def expected = buildEntity()
            def compositeId = compositeId(expected)
        and:
            repo.saveAll(seed())
            repo.save(expected)
        when:
            def result = repo.findOne(*compositeId)
        then:
            result == expected
    }

    def 'find approvals by userId and clientId'() {
        setup:
            def expected = [
                new PersistableApproval('flynn', 'abc', 'write'),
                new PersistableApproval('flynn', 'abc', 'read')
            ] as Set
        and:
            repo.saveAll(seed())
            repo.saveAll(expected)
        when:
            def results = repo.findByUserIdAndClientId('flynn', 'abc')
        then:
            results.size() == 2
            results as Set == expected
    }

    def 'whether approval exists by userId, clientId and scope'() {
        setup:
            def entity = buildEntity()
            def compositeId = compositeId(entity)
        and:
            assert ! repo.exists(*compositeId)
        when:
            repo.save(entity)
        then:
            repo.exists(*compositeId)
    }

    def 'delete approval by userId, clientId and scope'() {
        setup:
            def entity = buildEntity()
            def compositeId = compositeId(entity)

            repo.saveAll(seed())
            repo.save(entity)
        and:
            assert repo.exists(*compositeId)
        when:
            repo.deleteById(*compositeId)
        then:
            ! repo.exists(*compositeId)
    }

    def 'find valid approved scopes'() {
        setup:
            def wanted = [
                new PersistableApproval('flynn', 'abc', 'foo', true, now + 1),
                new PersistableApproval('flynn', 'abc', 'bar', true, now + 1),
            ]
            def others = [
                new PersistableApproval('flynn', 'abc', 'baz', false, now + 1),
                new PersistableApproval('flynn', 'abc', 'qux', true, now - 1),
                new PersistableApproval('flynn', 'fake', 'foo', true, now + 1),
                new PersistableApproval('clue', 'abc', 'foo', true, now + 1)
            ]
        and:
            repo.saveAll(wanted)
            repo.saveAll(others)
        when:
            def results = repo.findValidApprovedScopes('flynn', 'abc')
        then:
            results == wanted*.scope as Set
    }


    def getNow() {
        new Date()
    }

    private compositeId(PersistableApproval approval) {
        [approval.userId, approval.clientId, approval.scope]
    }
}

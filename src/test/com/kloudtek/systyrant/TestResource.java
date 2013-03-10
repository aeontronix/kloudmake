/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.annotation.*;
import com.kloudtek.systyrant.exception.ResourceCreationException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.resource.JavaResourceFactory;
import com.kloudtek.systyrant.resource.Resource;
import com.kloudtek.systyrant.resource.ResourceRef;
import com.kloudtek.systyrant.service.filestore.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.*;

@STResource("test:test")
public class TestResource {
    private static final Logger logger = LoggerFactory.getLogger(TestResource.class);
    private static AtomicInteger prepareOrderCounter = new AtomicInteger(0);
    private static AtomicInteger executeCounter = new AtomicInteger(0);
    private static AtomicInteger postChildrenCounter = new AtomicInteger(0);
    private String[] childs;
    private String value;
    private int prepareOrder;
    private int executeOrder;
    private Integer postChildrenOrder;
    private boolean hasChildrens;
    private boolean postExecReceived;
    @Attr
    private boolean failExecution;
    @Attr
    private boolean failPreparation;
    @Attr
    private boolean createElementDuringExecute;
    private String unique = UUID.randomUUID().toString();
    private ResourceRef ref;
    @Service
    private FileStore fileStore;
    @Inject
    private Resource resource;
    @Attr
    private String id;
    private Date prepared;
    private Date executed;
    private Date postChildrenExecuted;
    private Date cleanedUp;
    private boolean verifyGlobal;
    private boolean verifySpecific;

    static {
    }

    private Date verifyGlobalTS;
    private Date syncGlobalTS;
    private Date verifySpecificTS;
    private Date syncSpecificTS;

    @Prepare
    public void prepare() throws STRuntimeException {
        prepared = new Date();
        logger.info("{} : PREPARING", id);
        Assert.assertNotNull(resource, "resource is missing");
        Assert.assertNotNull(fileStore, "fileStore is missing");
        if (prepareOrder > 0) {
            Assert.fail("Compilation happening twice");
        }
        prepareOrder = prepareOrderCounter.incrementAndGet();
        if (childs != null) {
            for (String id : childs) {
                try {
                    Resource child = STHelper.createElement(TestResource.class, id);
                    child.setParent(resource);
                } catch (ResourceCreationException e) {
                    throw new STRuntimeException(e.getMessage());
                }
            }
        }
        if (failPreparation) {
            logger.info("{} : PREPARING FAILING AS SPECIFIED", id);
            throw new STRuntimeException("Failed prepare as requested");
        }
        logger.info("{} : PREPARING DONE", id);
    }

    @Verify
    public boolean verifyGlobal() {
        verifyGlobalTS = new Date();
        logger.info("{} : VERIFY GLOBAL {}", id, verifyGlobal);
        return verifyGlobal;
    }

    @Sync
    public void syncGlobal() {
        syncGlobalTS = new Date();
        logger.info("{} : SYNC GLOBAL {}", id);
    }

    @Verify("foo")
    public boolean verifySpecific() {
        verifySpecificTS = new Date();
        logger.info("{} : VERIFY SPECIFIC {}", id, verifySpecific);
        return verifySpecific;
    }

    @Sync("foo")
    public void syncSpecify() {
        syncSpecificTS = new Date();
        logger.info("{} : SYNC SPECIFIC {}", id);
    }

    @Execute
    public void execute() throws STRuntimeException {
        executed = new Date();
        executeOrder = executeCounter.incrementAndGet();
        logger.info("{} : EXECUTING (O={})", id, executeOrder);
        if (failExecution) {
            throw new STRuntimeException("Failed execution as requested");
        }
        if (createElementDuringExecute) {
            try {
                STHelper.createElement(TestResource.class, id + "-exechildren");
            } catch (ResourceCreationException e) {
                throw new STRuntimeException(e);
            }
        }
    }

    @Execute(postChildren = true)
    public void executePostChildren() throws STRuntimeException {
        postChildrenOrder = postChildrenCounter.incrementAndGet();
        postChildrenExecuted = new Date();
        postExecReceived = true;
        logger.info("{} : POST_CHILDREN_EXECUTE (O={})", id, postChildrenOrder);
    }

    @Cleanup
    public void cleanup() {
        cleanedUp = new Date();
        logger.info("{} : CLEANUP", id);
        assertNotNull(prepared);
        assertNotNull(verifyGlobalTS);
        if (verifyGlobal) {
            assertNotNull(syncGlobalTS);
        } else {
            assertNull(syncGlobalTS);
        }
        assertNotNull(verifySpecificTS);
        if (verifySpecific) {
            assertNotNull(syncSpecificTS);
        } else {
            assertNull(syncSpecificTS);
        }
        assertNotNull(executed);
    }

    public int getPrepareOrder() {
        return prepareOrder;
    }

    public void setPrepareOrder(int prepareOrder) {
        this.prepareOrder = prepareOrder;
    }

    public int getExecuteOrder() {
        return executeOrder;
    }

    public void setExecuteOrder(int executeOrder) {
        this.executeOrder = executeOrder;
    }

    public boolean isHasChildrens() {
        return hasChildrens;
    }

    public void setHasChildrens(boolean hasChildrens) {
        this.hasChildrens = hasChildrens;
    }

    public boolean isCreateElementDuringExecute() {
        return createElementDuringExecute;
    }

    public void setCreateElementDuringExecute(boolean createElementDuringExecute) {
        this.createElementDuringExecute = createElementDuringExecute;
    }

    public boolean isFailExecution() {
        return failExecution;
    }

    public TestResource setFailExecution(boolean failExecution) {
        this.failExecution = failExecution;
        return this;
    }

    public boolean isFailPreparation() {
        return failPreparation;
    }

    public TestResource setFailPreparation(boolean failPreparation) {
        this.failPreparation = failPreparation;
        return this;
    }

    @Unique
    public String getUnique() {
        return unique;
    }

    public void setUnique(String unique) {
        this.unique = unique;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ResourceRef getRef() {
        return ref;
    }

    public void setRef(ResourceRef ref) {
        this.ref = ref;
    }

    public Date getPrepared() {
        return prepared;
    }

    public Date getExecuted() {
        return executed;
    }

    public Date getPostChildrenExecuted() {
        return postChildrenExecuted;
    }

    public Integer getPostChildrenOrder() {
        return postChildrenOrder;
    }

    public void valFileResourceManagerInjected() {
        assertNotNull(fileStore);
    }

    public Date getSyncSpecificTS() {
        return syncSpecificTS;
    }

    public void setSyncSpecificTS(Date syncSpecificTS) {
        this.syncSpecificTS = syncSpecificTS;
    }

    public Date getVerifySpecificTS() {
        return verifySpecificTS;
    }

    public void setVerifySpecificTS(Date verifySpecificTS) {
        this.verifySpecificTS = verifySpecificTS;
    }

    public Date getSyncGlobalTS() {
        return syncGlobalTS;
    }

    public void setSyncGlobalTS(Date syncGlobalTS) {
        this.syncGlobalTS = syncGlobalTS;
    }

    public Date getVerifyGlobalTS() {
        return verifyGlobalTS;
    }

    public void setVerifyGlobalTS(Date verifyGlobalTS) {
        this.verifyGlobalTS = verifyGlobalTS;
    }

    public boolean isVerifySpecific() {
        return verifySpecific;
    }

    public void setVerifySpecific(boolean verifySpecific) {
        this.verifySpecific = verifySpecific;
    }

    public boolean isVerifyGlobal() {
        return verifyGlobal;
    }

    public void setVerifyGlobal(boolean verifyGlobal) {
        this.verifyGlobal = verifyGlobal;
    }

    public static void valDeps(Resource resource, Resource... expectedDeps) {
        ArrayList<Resource> deps = new ArrayList<>(resource.getResolvedDeps());
        for (Resource el : expectedDeps) {
            assertNotNull(deps.remove(el));
        }
        assertTrue(deps.isEmpty());
    }

    public static TestResource get(Resource el) {
        for (STAction action : el.getActions()) {
            if (action instanceof JavaResourceFactory.JavaImpl) {
                return (TestResource) ((JavaResourceFactory.JavaImpl) action).getImpl();
            }
        }
        return null;
    }

    public static void createChild(Resource parent, String... childrens) {
        TestResource testResource = TestResource.get(parent);
        testResource.childs = childrens;
        if (childrens != null && childrens.length > 0) {
            testResource.hasChildrens = true;
        }
    }
}

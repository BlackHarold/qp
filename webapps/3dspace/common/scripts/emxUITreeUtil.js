/*=================================================================
 *  JavaScript Tree Utilities
 *  emxUITreeUtil.js
 *  Version 1.0
 *  Requires: emxUIConstants.js, emxUICoreTree.js
  *
 *  This file contains utility methods for interfacing with the
 *  structure and details trees.
 *
 *  Copyright (c) 1992-2018 Dassault Systemes. All Rights Reserved.
 *  This program contains proprietary and trade secret information
 *  of MatrixOne,Inc. Copyright notice is precautionary only
 *  and does not evidence any actual or intended publication of such program
 *
 *  static const char RCSID[] = $Id: emxUITreeUtil.js.rca 1.12 Wed Oct 22 15:47:52 2008 przemek Experimental przemek $
 *=================================================================
 */

//-----------------------------------------------------------------
// Public Function addStructureTreeNode()
//-----------------------------------------------------------------
// BROWSER(S)
//      Internet Explorer 5.0+
//      Netscape Navigator 4.x
//      Mozilla 1.0.1+ (Netscape 7.0+)
//
// AUTHOR(S)
//      John Rajan, (JR) 12-Feb-03
//
// EDITOR(S)
//      Nicholas C. Zakas, (NCZ) 13-Feb-03
//
// DESCRIPTION
//      This function to add new node to the Structure tree and updates the Context tree. This function
//      is considered PUBLIC and may be used by developers.
//
// PARAMETERS
//      strObjectId (String) - The OID of the new object added  (JR, 12-Feb-03)
//      strParentId (String) - OID of the parent object to which the new object is connected  (JR, 12-Feb-03)
//      strTreeId (String) - input jsTreeID URL paramter, passed from tree - current selected node  (JR, 12-Feb-03)
//      strSuiteDirectory (String) - The app directory of the suite  (JR, 12-Feb-03)
//      blnAddToDetailsTree (Boolean) - whether to add to the details tree as well (optional).
//
// RETURNS
//      (nothing)
//-----------------------------------------------------------------
function addStructureTreeNode(strObjectId, strParentId, strTreeId, strSuiteDirectory, blnAddToDetailsTree, strRelId, blnRefresh, isChildNodeSetActive) {
    if (objStructureFancyTree && objStructureFancyTree.isActive) {
        objStructureFancyTree.addChild(strParentId, strObjectId, isChildNodeSetActive);
    } else {
        if (getTopWindow().objStructureTree && blnRefresh) {
            var objFrame = getTopWindow().findFrame(getTopWindow(),
                "detailsDisplay");
            objFrame.document.location.href = objFrame.document.location.href;
        }
    }

} // End: function addStructureTreeNode(strObjectId, strParentId, strTreeId,
// strSuiteDirectory)

//-----------------------------------------------------------------
// DEPRECATED Function addStructureNode()
//-----------------------------------------------------------------
// BROWSER(S)
//      Internet Explorer 5.0+
//      Netscape Navigator 4.x
//      Mozilla 1.0.1+ (Netscape 7.0+)
//
// AUTHOR(S)
//      John Rajan, (JR) 12-Feb-03
//
// EDITOR(S)
//      Nicholas C. Zakas, (NCZ) 13-Feb-03
//
// DESCRIPTION
//      This function to add new node to the Structure tree and updates the Details tree.
//      DEPRECATED - use addStructureTreeNode() instead.
//
// PARAMETERS
//      strObjectId (String) - The OID of the new object added  (JR, 12-Feb-03)
//      strParentId (String) - OID of the parent object to which the new object is connected  (JR, 12-Feb-03)
//      strTreeId (String) - input jsTreeID URL paramter, passed from tree - current selected node  (JR, 12-Feb-03)
//      strSuiteDirectory (String) - The app directory of the suite  (JR, 12-Feb-03)
//      blnAddToDetailsTree (Boolean) - whether to add to the details tree as well (optional).
//
// RETURNS
//      (nothing)
//-----------------------------------------------------------------
function addStructureNode(strObjectId, strParentId, strTreeId, strSuiteDirectory, blnAddToDetailsTree, strRelId, blnRefresh, isChildNodeSetActive) {
    addStructureTreeNode(strObjectId, strParentId, strTreeId, strSuiteDirectory, blnAddToDetailsTree, strRelId, blnRefresh, isChildNodeSetActive);
} //End: function addStructureNode(strObjectId, strParentId, strTreeId, strSuiteDirectory)

//-----------------------------------------------------------------
// Public Function addMultipleStructureNodes()
//-----------------------------------------------------------------
// BROWSER(S)
//      Internet Explorer 5.0+
//      Netscape Navigator 4.x
//      Mozilla 1.0.1+ (Netscape 7.0+)
//
// AUTHOR(S)
//      Jean Binjour, (JB) 6/23/03
//
// EDITOR(S)
//
//
// DESCRIPTION
//      This function to add new node to the Structure tree and updates the Context tree. This function
//      is considered PUBLIC and may be used by developers.
//
// PARAMETERS
//      strObjectId (String) - The OID List, comma seperated  (JB) 6/23/03
//      strParentId (String) - OID of the parent object to which the new object is connected  (JB) 6/23/03
//      strTreeId (String) - input jsTreeID URL paramter, passed from tree - current selected node  (JB) 6/23/03
//      strSuiteDirectory (String) - The app directory of the suite  (JB) 6/23/03
//      blnAddToDetailsTree (Boolean) - whether to add to the details tree as well (optional).
//
// RETURNS
//      (nothing)
//-----------------------------------------------------------------
function addMultipleStructureNodes(strObjectId, strParentId, strTreeId, strSuiteDirectory, blnAddToDetailsTree) {
    addStructureNode(strObjectId, strParentId, strTreeId, strSuiteDirectory, false, null, null, false)
} //End: function addStructureNode(strObjectId, strParentId, strTreeId, strSuiteDirectory)


//-----------------------------------------------------------------
// Public Function addRelationshipTo()
//-----------------------------------------------------------------
// BROWSER(S)
//      Internet Explorer 5.0+
//      Netscape Navigator 4.x
//      Mozilla 1.0.1+ (Netscape 7.0+)
//
// AUTHOR(S)
//      Jean Binjour, (JB) 6/23/03
//
// EDITOR(S)
//
//
// DESCRIPTION
//      This function add a new node to all nodes with the passed in objectid
//
// PARAMETERS
//      strObjectId (String) - The OID used to find the structure tree nodes  (JB) 6/23/03
//
// RETURNS
//      (nothing)
//-----------------------------------------------------------------
function addRelationshipTo(strObjectId) {
    if (getTopWindow().objStructureTree) {
        var objStructureTree = getTopWindow().objStructureTree;
        var objNode = objStructureTree.findNodeByObjectID(strObjectId);
        objStructureTree.addRelationshipTo(objNode);
    }

} //End: function addRelationshipTo(strObjectId, strParentId, strTreeId, strSuiteDirectory)


//-----------------------------------------------------------------
// Public Function removeRelationshipTo()
//-----------------------------------------------------------------
// BROWSER(S)
//      Internet Explorer 5.0+
//      Netscape Navigator 4.x
//      Mozilla 1.0.1+ (Netscape 7.0+)
//
// AUTHOR(S)
//      Jean Binjour, (JB) 6/23/03
//
// EDITOR(S)
//
//
// DESCRIPTION
//      This function removes all nodes with the passed in objectid
//
// PARAMETERS
//      strObjectId (String) - The OID used to find the structure tree nodes  (JB) 6/23/03
//
// RETURNS
//      (nothing)
//-----------------------------------------------------------------
function removeRelationshipTo(strObjectId) {
    if (getTopWindow().objStructureTree) {
        var objStructureTree = getTopWindow().objStructureTree;
        var objNode = objStructureTree.findNodeByObjectID(strObjectId);
        objStructureTree.removeRelationshipTo(objNode);
    }

} //End: function removeRelationshipTo(strObjectId, strParentId, strTreeId, strSuiteDirectory)


//-----------------------------------------------------------------
// Public Function addDetailsTreeNode()
//-----------------------------------------------------------------
// BROWSER(S)
//      Internet Explorer 5.0+
//      Netscape Navigator 4.x
//      Mozilla 1.0.1+ (Netscape 7.0+)
//
// AUTHOR(S)
//      John Rajan, (JR) 12-Feb-03
//
// EDITOR(S)
//      Nicholas C. Zakas (NCZ), 13-Feb-03
//
// DESCRIPTION
//      This function to add new node to the Details tree. This function
//      is considered PUBLIC and may be used by developers.
//
// PARAMETERS
//      strObjectId (String) -  OID of the new object added  (JR, 12-Feb-03)
//      strTreeId (String) - input jsTreeID URL paramter, passed from tree - current selected node  (JR, 12-Feb-03)
//      strSuiteDirectory (String) - The app directory of the suite  (JR, 12-Feb-03)
//
// RETURNS
//      (nothing)
//method code is removed since V6R2014x for Function_041714 .method is retained since it is public and is used by other applications
//-----------------------------------------------------------------
function addDetailsTreeNode(strObjectId, strTreeId, strSuiteDirectory) {

} //End: function addDetailsTreeNode(strObjectId, strTreeId, strSuiteDirectory)

//-----------------------------------------------------------------
// DEPRECATED Function addContextTreeNode()
//-----------------------------------------------------------------
// BROWSER(S)
//      Internet Explorer 5.0+
//      Netscape Navigator 4.x
//      Mozilla 1.0.1+ (Netscape 7.0+)
//
// AUTHOR(S)
//      John Rajan, (JR) 12-Feb-03
//
// EDITOR(S)
//      Nicholas C. Zakas (NCZ), 13-Feb-03
//
// DESCRIPTION
//      This function to add new node to the Context tree. 
//      DEPRECATED - use addDetailsTreeNode() instead.
//
// PARAMETERS
//      strObjectId (String) -  OID of the new object added  (JR, 12-Feb-03)
//      strTreeId (String) - input jsTreeID URL paramter, passed from tree - current selected node  (JR, 12-Feb-03)
//      strSuiteDirectory (String) - The app directory of the suite  (JR, 12-Feb-03)
//
// RETURNS
//      (nothing)
//-----------------------------------------------------------------
function addContextTreeNode(strObjectId, strTreeId, strSuiteDirectory) {
    addDetailsTreeNode(strObjectId, strTreeId, strSuiteDirectory);
} //End: function addContextTreeNode(strObjectId, strTreeId, strSuiteDirectory)

//-----------------------------------------------------------------
// Public Function deleteObjectFromTrees()
//-----------------------------------------------------------------
// BROWSER(S)
//      Internet Explorer 5.0+
//      Netscape Navigator 4.x
//      Mozilla 1.0.1+ (Netscape 7.0+)
//
// AUTHOR(S)
//      John Rajan, (JR) 12-Feb-03
//
// EDITOR(S)
//      Nicholas C. Zakas (NCZ), 13-Feb-03
//
// DESCRIPTION
//      This function deletes the object with the given object ID
//      from both the details tree and the structure tree. This function
//      is considered PUBLIC and may be used by developers.
//
// PARAMETERS
//      strObjectId (String) - OID of the object to be deleted. (JR, 12-Feb-03)
//      blnRefresh (boolean) - should the trees be refreshed?. (JR, 12-Feb-03)
//
// RETURNS
//      (nothing)
//-----------------------------------------------------------------
function deleteObjectFromTrees(strObjectId, blnRefresh, unLinkFolder) {

    if (objStructureFancyTree && objStructureFancyTree.isActive) {
        if (unLinkFolder) {
            var rootnode = objStructureFancyTree.getRootNode();
            objStructureFancyTree.removeChild(strObjectId, rootnode);
        } else {
            objStructureFancyTree.removeChild(strObjectId);
        }

    }
    if (getTopWindow().bclist) {
        getTopWindow().bclist.remove(strObjectId);
    }

} //End: function deleteObjectFromTrees(strObjectId, blnRefresh)

//-----------------------------------------------------------------
// DEPRECATED Function deleteStructureNode()
//-----------------------------------------------------------------
// BROWSER(S)
//      Internet Explorer 5.0+
//      Netscape Navigator 4.x
//      Mozilla 1.0.1+ (Netscape 7.0+)
//
// AUTHOR(S)
//      John Rajan, (JR) 12-Feb-03
//
// EDITOR(S)
//      Nicholas C. Zakas (NCZ), 13-Feb-03
//
// DESCRIPTION
//      This function deletes the object with the given object ID
//      from both the details tree and the structure tree. 
//      DEPRECATED - use deleteObjectFromTrees() instead.
//
// PARAMETERS
//      strObjectId (String) - OID of the object to be deleted. (JR, 12-Feb-03)
//      blnRefresh (boolean) - should the tree be refreshed?. (JR, 12-Feb-03)
//
// RETURNS
//      (nothing)
//-----------------------------------------------------------------
function deleteStructureNode(strObjectId, blnRefresh) {
    deleteObjectFromTrees(strObjectId, blnRefresh);
} //End: function deleteStructureNode(strObjectId, blnRefresh)

//-----------------------------------------------------------------
// Public Function deleteMultipleStructureNode()
//-----------------------------------------------------------------
// BROWSER(S)
//      Internet Explorer 5.0+
//      Netscape Navigator 4.x
//      Mozilla 1.0.1+ (Netscape 7.0+)
//
// AUTHOR(S)
//      Jean Binjour, (JB) 6/23/03
//
// EDITOR(S)
//
//
// DESCRIPTION
//      This function deletes multiple nodes at a time.
//
// PARAMETERS
//      strObjectId (String) - OID List to be deleted. (JB) 6/23/03
//      blnRefresh (boolean) - should the tree be refreshed?. (JB) 6/23/03
//
// RETURNS
//      (nothing)
//-----------------------------------------------------------------
function deleteMultipleStructureNode(strObjectId) {
    if (strObjectId != null && strObjectId.length > 0) {
        var arrObjIds = strObjectId.split(",");
        //set refresh flag
        //blnRefresh = (blnRefresh == null ? true : blnRefresh);
        for (var z = 0; z < arrObjIds.length; z++) {
            //get an object id
            var sObjId = arrObjIds[z];
            if (getTopWindow().objStructureTree) {
                // If selectedNode is not null, then only invoke method "removeRelationshipTo" on it.
                var selectedNodeObj = getTopWindow().objStructureTree.getSelectedNode();
                if (selectedNodeObj) {
                    selectedNodeObj.removeRelationshipTo(sObjId);
                }
            } else {
                if (getTopWindow().bclist) {
                    getTopWindow().bclist.remove(sObjId);
                }
            }
        }
    }
} //End: function deleteStructureNode(strObjectId, blnRefresh)

//-----------------------------------------------------------------
//Public Function changeObjectLabelInTree()
//-----------------------------------------------------------------
//BROWSER(S)
//
//AUTHOR(S)
//    Senthil Rajagopal, (SL9) 09/14/10
//
//EDITOR(S)
//
//
//DESCRIPTION
//   This function is to change the label of the object nodes
//   In case of new UI, there is no details tree and hence no updates are done to details tree. 
//
//PARAMETERS
//   strObjectId (String) - The OID of the object to be modified  (JR, 12-Feb-03)
//   strLabel (String) - The new label for the object  (JR, 12-Feb-03)
//   blnChangeDetailsTree (Boolean) - whether to add to the details tree as well (optional).
//   blnRefresh (Boolean) - whether to refresh the trees (optional).
//
//RETURNS
//   (nothing)
//-----------------------------------------------------------------
function changeObjectLabelInTree(strObjectId, strLabel, blnChangeDetailsTree, blnRefreshStructureTree, blnRefreshDetailsTree) {
    var currbc = getTopWindow().bclist.getCurrentBC();
    var currWindow = getTopWindow();
    if (currbc === null) {
        currbc = currWindow.getWindowOpener().getTopWindow().bclist.getCurrentBC();
        currWindow = getTopWindow().getWindowOpener().getTopWindow();
    }
    if (objStructureFancyTree && objStructureFancyTree.isActive) {
        objStructureFancyTree.setTitle(strObjectId, strLabel);
    }

    if (currWindow.bclist && !objStructureFancyTree && currbc.id == strObjectId) {
        currWindow.bclist.changeLabel(strObjectId, strLabel);
    }

    if (currbc!=null) {
        if (strObjectId == currbc.id) {
            //Changing the label of category tab
            var currbc = currWindow.bclist.getCurrentBC();
            if (currbc && currbc.categoryObj && strLabel != null && strLabel != "") {
                try {
                    currbc.categoryObj.items[0].text = strLabel;
                } catch (err) {
                }
                currWindow.emxUICategoryTab.changeRootLabel(strLabel);
            }
        }
    }
}

//-----------------------------------------------------------------
//Public Function changeObjectIDInTree()
//-----------------------------------------------------------------
//BROWSER(S)
//
//AUTHOR(S)
//  Senthil Rajagopal, (SL9) 09/14/10
//
//EDITOR(S)
//
//
//DESCRIPTION
// This function is to change the ID of the object nodes
// In case of new UI, there is no details tree and hence no updates are done to details tree. 
//
//PARAMETERS
// strObjectId (String) - The OID of the object to be modified  (JR, 12-Feb-03)
// strNewId (String) - The new id for the object  (JR, 12-Feb-03)
// blnChangeDetailsTree (Boolean) - whether to add to the details tree as well (optional).
// blnRefresh (Boolean) - whether to refresh the trees (optional).
//
//RETURNS
// (nothing)
//-----------------------------------------------------------------
function changeObjectIDInTree(strObjectId, strNewId, blnChangeDetailsTree, blnRefresh) {
    if (objStructureFancyTree && objStructureFancyTree.isActive) {
        objStructureFancyTree.setObjectId(strObjectId, strNewId);
    }
    if (getTopWindow().bclist) {
        getTopWindow().bclist.changeID(strObjectId, strNewId);
    }
}

//-----------------------------------------------------------------
//DESCRIPTION
//   This function draws/refreshes structure tree. 
//
//PARAMETERS
//   frameStructure (Window) - Window where structure tree should be drawn. If variable is 
//                             not specified, structure tree drawn in emxUIStructureTree (optional)
//
//RETURNS
//   (nothing)
//-----------------------------------------------------------------
function refreshStructureTree(frameStructure) {
    if (objStructureFancyTree && objStructureFancyTree.isActive) {
        objStructureFancyTree.refreshTree();
    }
}

//-----------------------------------------------------------------
//DESCRIPTION
//   This function draws/refreshes Details tree. In new UI details tree is not present and hence no refresh is done.
//
//PARAMETERS
//   (nothing)
//
//RETURNS
//   (nothing)
//-----------------------------------------------------------------
function refreshDetailsTree(frameToRefresh) {
    // code to refresh the content page in new UI

    if (frameToRefresh) {
        frameToRefresh.location.href = frameToRefresh.location.href;
    } else {
        var wndContent = getTopWindow().findFrame(getTopWindow(), "detailsDisplay");
        if (wndContent) {
            wndContent.location.href = wndContent.location.href;
        } else {
// this code is added because on move operation 
            var contFrame = openerFindFrame(getTopWindow(), "detailsDisplay")
            contFrame.location.href = contFrame.location.href;
        }
    }

}

//-----------------------------------------------------------------
//DESCRIPTION
//   This function refreshes both structure tree and details tree. In new UI details tree is not present and hence no refresh is done.
//
//PARAMETERS
//   (nothing)
//
//RETURNS
//   (nothing)
//-----------------------------------------------------------------
function refreshTrees(frameToRefresh) {
    refreshStructureTree();
    refreshDetailsTree(frameToRefresh);
}


   

/**
 * Initialization routines for Structure Browser to communicate with the Tag Navigator
 * @requres jQuery
 * @version 1.0
 */

if (!bpsTagNavSBInit) {


    var bpsTagNavSBInit = {
        tagXML: null,
        aSBRowNodes: null,
        aSBRowNodesChecked: null,
        tnID: null,
        oidMapping: {},

        handleFilter: function (objSelection) {
            //apply selection (objSelection) to the tagXML
            if (this.tagXML) {
                this.showObjects(oXML, objSelection.filteredSubjectList);
                processRowGroupingToolbarCommand();
                var sbType = emxUICore.selectSingleNode(oXML, "/mxRoot/requestMap/setting[@name = 'sbType']");
                var pageSize = emxUICore.selectSingleNode(oXML, "/mxRoot/requestMap/setting[@name = 'pageSize']");//getParameter('pageSize');
                if ((objSelection.filteredSubjectList.length > 0) || (pageSize && "FlatMode" == emxUICore.getText(sbType))) {
                    rebuildView();
                }
            }
        },

        showObjects: function (objXml, filteredSubjectList) {
            var rows = emxUICore.selectNodes(objXml, "/mxRoot/rows//r[not(@calc) and not(@rg) and not(@status)]");
            if (rows.length == 0) {
                return;
            }
            for (var i = 0; i < rows.length; i++) {
                var row = rows[i];
                var objectId = row.getAttribute("o");
                var pid = this.oidMapping[objectId];
                var filter = true;
                if (bpsSBFilter.__findInArray(filteredSubjectList, pid) != -1 || (typeof pid == "undefined")) {
                    filter = false;
                    row.setAttribute("tagfiltered", "false");
                } else {
                    row.setAttribute("tagfiltered", "true");
                }
                bpsSBFilter.__setRowFilter(row, filter);
            }
        },

        handleFilterFTS: function (objSelection) {
            //apply selection (objSelection) to the tagXML
            FullSearch.selectedSearch = null;
            FullSearch.formSearchTimeOut(objSelection);
        },

        resetFilterFTS: function (objSelection) {
            FullSearch.filters = emxUICore.parseJSON(FullSearch.initialFilterJSONString);
            FullSearch.formSearchTimeOut();
        },

        handleDrawTags: function () {
            var aSBRowNodes = [];
            //get oids from oXML
            this.aSBRowNodes = jQuery("r[o]", oXML).not("[rg]").not("[calc]");
            jQuery(this.aSBRowNodes).map(function (index) {
                aSBRowNodes[index] = this.getAttribute("o");
            });

            function processData(objTagXml) { // inner function don't use "this"
                //store tagXML
                bpsTagNavSBInit.tnID.unsetTags();
                bpsTagNavSBInit.tagXML = objTagXml;
                //build 6w tag object
                var tagObj = bpsTagNavSBInit.buildJSONTagData(objTagXml, aSBRowNodes);
                //load tag nav
                bpsTagNavSBInit.tnID.setSubjectsTags(JSON.parse(JSON.stringify(tagObj)));
                bpsTagNavSBInit.handleTagCollect();
            }

            if (aSBRowNodes.length > 0) {
                //get tag data from service
                var postData = "oid_list=" + aSBRowNodes.join(",");
                emxUICore.getXMLDataPost("../resources/bps/sbtagdata/sbtagdata", postData, processData, null);
            } else {
                //unload
                bpsTagNavSBInit.tnID.setSubjectsTags({});
            }
        },

        handleTagCollect: function () {
            var aSBRowNodes = [];
            //get oids from oXML
            var aSBRowNodesChecked = emxUICore.selectNodes(oXML, "/mxRoot/rows//r[@checked = 'checked' and not(@calc) and not(@rg) and (not(@tagfiltered) or @tagfiltered='false')]");
            jQuery(aSBRowNodesChecked).map(function (index) {
                var objectId = this.getAttribute("o")
                var pid = bpsTagNavSBInit.oidMapping[objectId];
                if (pid) {
                    aSBRowNodes.push(pid);
                }
            });
            if (aSBRowNodes.length > 0) {
                this.aSBRowNodesChecked = aSBRowNodesChecked;
                bpsTagNavSBInit.tnID.focusOnSubjects(aSBRowNodes);
            } else {
                if (this.aSBRowNodesChecked != null) {
                    bpsTagNavSBInit.tnID.unfocus();
                }
            }
        },

        buildJSONTagData: function (tagXML, aSBRowNodes) {
            var columnList = null;
            if (aSBRowNodes != null) {
                columnList = [];
                var columns = emxUICore.selectNodes(tagXML, "/mxRoot/columns/column");
                for (var i = 0; i < columns.length; i++) {
                    var col = columns[i];
                    var colName = col.getAttribute("name");
                    var colField = col.getAttribute("label");
                    var predicate = null;
                    if (colField == null) {
                        predicate = colName.substring(0, colName.length - 1);
                    } else {
                        predicate = colName.replace("/" + colField, "");
                    }
                    //in weblogic, the xml is not passing settings; working around this issue.
                    //var sixw = emxUICore.selectSingleNode(col, 'settings/setting[@name="sixw"]');
                    //var predicate = emxUICore.getText(sixw);
                    var colObj = {
                        "name": colName,
                        "label": colField,
                        "sixw": predicate
                    };
                    columnList.push(colObj);
                }
                this.columnList = columnList;
            } else {
                columnList = this.columnList;
            }

            var tagData = {};
            var rows = emxUICore.selectNodes(tagXML, "/mxRoot/rows/r");
            var tag, cells, colObj, value, j, k, values;
            for (var i = 0; i < rows.length; i++) {
                var row = rows[i];
                var pid = row.getAttribute("o");
                if (aSBRowNodes != null) {
                    var oid = aSBRowNodes[i];
                    this.oidMapping[oid] = pid;
                }
                if (row.getAttribute("filter") == "true") continue;
                tags = [];

                cells = row.getElementsByTagName("c");
                for (j = 0; j < cells.length; j++) {
                    colObj = columnList[j];
                    value = emxUICore.getText(cells[j]);
                    values = value.split(bpsSBFilter.multiValueSeparator);
                    for (k = 0; k < values.length; k++) {
                        if (values[k] == "") continue;
                        if (values[k].contains("/")) {
                            var s = values[k].split("/");
                            tag = {
                                "object": s,
                                "dispObject": s,
                                "sixw": colObj.sixw,
                                "field": colObj.label
                            };
                        } else {
                            tag = {
                                "object": values[k],
                                "dispObject": values[k],
                                "sixw": colObj.sixw,
                                "field": colObj.label
                            };
                        }
                        tags.push(tag);
                    }
                }
                tagData[pid] = tags;
            }

            return tagData;
        },

        init: function () {
            if (typeof FullSearch == "undefined" && !fullTextSearchObj) {
                var tagger, oThis = this, path;
                topAccessWin = bpsTagNavConnector.getTNWindow();

                if (topAccessWin) {
                    path = window.location.pathname.split('/')[1];
                    tagServiceURL = window.location.protocol + "//" + window.location.host + "/" + path;

                    require(['DS/TagNavigatorProxy/TagNavigatorProxy'], function (TagNavigatorProxy) {
                        topAccessWin.jQuery(topAccessWin.document).bind('TN_LAUNCHED', oThis.handleDrawTags);
                        if (!oThis.tnID) {
                            var paramWidgetId = getTopWindow().location.search.match(/[?&]widgetId=([^&]*)?/),
                                paramTenant = getTopWindow().location.search.match(/[?&]tenant=([^&]*)?/);
                            paramWidgetId = (paramWidgetId == null ? undefined : paramWidgetId[1] || undefined);
                            paramTenant = (paramTenant == null ? undefined : paramTenant[1] || undefined);
                            paramTenant = (paramTenant == null ? (topAccessWin.curTenant == "" ? "OnPremise" : topAccessWin.curTenant) : paramTenant);
                            if (typeof getTopWindow().taggerCtx === "undefined") {
                                getTopWindow().taggerCtx = "context1";
                            }
                            var options = {
                                widgetId: paramWidgetId,
                                contextId: paramWidgetId == null ? getTopWindow().taggerCtx : undefined,
                                tenant: paramTenant == "onpremise" ? undefined : paramTenant,
                                filteringMode: 'WithFilteringServices'
                            };
                            if (!getTopWindow().isfromIFWE && emxUIConstants.TOPFRAME_ENABLED) {

                                tagger = getTopWindow().topFrameTagger.get6WTagger(getTopWindow().taggerCtx);
                                tagger.setAsCurrent();
                            }

                            //setup listeners
                            //when a tag is clicked in TN
                            oThis.tnID = TagNavigatorProxy.createProxy(options);
                            oThis.tnID.toggleProxyFocus("true");
                            oThis.tnID.addFilterSubjectsListener(oThis.handleFilter, oThis, false);

                            //when SB loads or changes data
                            console.log("setting sb listeners");
                            topAccessWin.jQuery(topAccessWin.document).bind('sb_data_changed.bps_sb', oThis.handleDrawTags);
                            topAccessWin.jQuery(topAccessWin.document).bind('sb_selection_changed.bps_sb', oThis.handleTagCollect);

                            removeTagger = function () {
                                oThis.tnID.die();
                            }

                            destroySBListeners = function () {
                                console.log("destroying sb listeners");
                                topAccessWin.jQuery(topAccessWin.document).unbind(".bps_sb");
                                topAccessWin.jQuery(topAccessWin.document).unbind('TN_LAUNCHED');
                                //if(topAccessWin.emxUISlideIn && topAccessWin.emxUISlideIn.current_slidein && topAccessWin.emxUISlideIn.current_slidein.dir == "left"){
                                //topAccessWin.closeSlideInDialog();
                                //topAccessWin.showSlideInDialog.mode = "";
                                //}
                                if (topAccessWin.bpsTagNavConnector && topAccessWin.bpsTagNavConnector.TagNavigator) {
                                    topAccessWin.bpsTagNavConnector.rebuildViewNeeded = false;
                                    topAccessWin.bpsTagNavConnector.TagNavigator.get6WTagger(getTopWindow().taggerCtx).clearFilters(true);
                                } else if (emxUIConstants.TOPFRAME_ENABLED) {
                                    /** fix by task # 51293
                                     commented out the next one line */
                                    // getTopWindow().topFrameTagger.get6WTagger(getTopWindow().taggerCtx).clearFilters(true);
                                }
                            };
                            if (getTopWindow().isMobile) {
                                jQuery(window).bind('unload', destroySBListeners);
                                jQuery(window).bind('unload', removeTagger);
                            } else {
                                jQuery(window).bind('beforeunload', removeTagger);
                                jQuery(window).bind('unload', destroySBListeners);
                            }


                        } else {
                            oThis.tnID.activate();
                        }
                        top.removeEventListener('onTopFrameLoad', bpsTagNavSBInit.loadInit, false);
                    });
                }
            }
        },

        loadInit: function () {
            jQuery(function () {
                bpsTagNavSBInit.init();
            });
        }
    };

    if (!console) { //prevent IE from throwing errors. This is just a debug utility
        console = {
            log: function () {
            },
            warn: function () {
            },
            dir: function () {
            }
        };
    }


    var topbar = top.document.getElementById("topbar");
    if (!topbar) {
        top.addEventListener('onTopFrameLoad', bpsTagNavSBInit.loadInit, false);
    } else {
        bpsTagNavSBInit.loadInit();
    }

}

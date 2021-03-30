/*!================================================================
 *  JavaScript Toolbar Component
 *  emxUIToolbar.js
 *  Version 1.4.2
 *  Requires: emxUICore.js, emxUICoreMenu.js
 *
 *  This file contains the definition of the toolbar for use in
 *  DOM-compliant Web browsers such as IE and Mozilla. Any page
 *  that uses this file should also link to emxUIToolbar.css
 *  for the appropriate style to show up.
 *
 *  Copyright (c) 1992-2018 Dassault Systemes. All Rights Reserved.
 *=================================================================
 */
var toolbars = new Array;
toolbars.setListLinks = function _toolbars_setListLinks(blnListLinks, uiType, SBMode) {
    for (var i = 0; i < this.length; i++) {
        this[i].setListLinks(blnListLinks, uiType, SBMode);
    }
};
toolbars.init = function _toolbars_init(strID, bSearch) {
    for (var i = 0; i < this.length; i++) {
        if (this.redraw) {
            this[i].redraw = true;
        }
        if (this[i].overflow) {
            this[i].overflow = false;
            this[i].items.pop();
        }
        if (i > 1) {
            this[i].init("div" + i);
        } else {
            this[i].init(strID);
        }
    }
    if (bSearch && (typeof emxEditableTable !== 'undefined')) {
        var findInCntrl = new emxFindInCntrl(strID);
        emxEditableTable.findInCntrl = findInCntrl;
    }

    //If toolbar folding occurs, then height of the toolbar frame is doubled
    if ((toolbars[1] && toolbars[1].element != null && toolbars[0].element != null) &&
        ((toolbars[1].element.offsetWidth + toolbars[0].element.offsetWidth) > (toolbars[1].container.offsetWidth - 8))) {
        //toolbars[1].container.className += " folded";
        setTimeout(function () {
            var displayFrame = findFrame(parent, "listDisplay");
            if (displayFrame != null) {
                var tableElements = displayFrame.document.getElementsByName("table");
                var tableName = "";
                if (tableElements != null && tableElements[0] != null) {
                    tableName = tableElements[0].value;
                }
                var fs = parent.document.getElementById(tableName + "_fs");
                if (fs != null) {
                    var rows = parent.document.getElementById(tableName + "_fs").rows.split(",");
                    rows[0] = parseInt(rows[0]) + 21;
                    var adjustRows = rows.join(",");
                    fs.rows = adjustRows;
                }
            }

        }, 100);//REVIST 800- 100

    }
};
//! Class emxUIToolbar
//!     This object represents an toolbar.
function emxUIToolbar(blnCrossFrames, name) {
    this.superclass = emxUIObject;
    this.superclass();
    delete this.superclass;
    this.mode = 0;
    this.historyEnabled = false;
    this.emxClassName = "emxUIToolbar";
    this.items = new Array;
    this.element = null;
    this.maxWidth = emxUIToolbar.MAX_WIDTH;
    this.maxLabelChars = -1;
    this.width = -1;
    this.stylesheet = emxUIToolbar.CSS_FILE;
    toolbars[toolbars.length] = this;
    this.name = name;
    this.overflow = false;
}

emxUIToolbar.prototype = new emxUIObject;
emxUIToolbar.POS_TOP = 57;
emxUIToolbar.POS_LEFT = 10;
emxUIToolbar.DELAY_BETWEEN = 50;
emxUIToolbar.SCROLL_DISTANCE = 10;
emxUIToolbar.ICON_ONLY = 0;
emxUIToolbar.TEXT_ONLY = 1;
emxUIToolbar.ICON_AND_TEXT = 2;
emxUIToolbar.IMG_MENU_ARROW = emxUIConstants.DIR_IMAGES + "utilMenuArrow.gif";
emxUIToolbar.IMG_CHEVRON = emxUIConstants.DIR_IMAGES + "utilToolbarChevron.gif";
emxUIToolbar.CSS_FILE = emxUICore.getStyleSheet("emxUIToolbar");
emxUIToolbar.MODE_NORMAL = 0;
emxUIToolbar.MODE_FRAMES = 1;
emxUIToolbar.MODE_PORTAL = 2;
emxUIToolbar.MAX_WIDTH = -1;
emxUIToolbar.UI_AUTOMATION = emxUIConstants.UI_AUTOMATION;

// Added for Toolbar enhancement code - Begin

emxUIToolbar.INPUT_TYPE_TEXTBOX = "textbox";
emxUIToolbar.INPUT_TYPE_COMBOBOX = "combobox";
emxUIToolbar.INPUT_TYPE_SUBMIT = "submit";
emxUIToolbar.INPUT_TYPE_CHECKBOX = "checkbox";
var noOfButtons = 0;

// Added for Toolbar enhancement code - End

//! Public Method emxUIToolbar.addItem()
//!     This method adds an item to the toolbar.
emxUIToolbar.prototype.addItem = function _emxUIToolbar_addItem(objItem, blnListLink) {
    this.items.push(objItem);
    objItem.parent = this;
    objItem.listLink = !!blnListLink;
    objItem.toolbar = this;
    return objItem;
};

emxUIToolbar.prototype.drawToolbar = function _emxUIToolbar_drawToolbar(div) {

    var divToolbar = document.getElementById(div);
    if (!divToolbar) {
        return;
    }

    if (this.overflow) {
        this.overflow = false;
        while (this.items[this.items.length - 1].emxClassName == 'emxUIToolbarOverflowButton') {
            this.items.pop();
        }
    }

    var frmvalmap = new HashMap();
    var inpelems = document.getElementsByTagName("input");
    for (var k = 0; k < inpelems.length; k++) {
        if (inpelems[k].type == "hidden" || inpelems[k].type == "textbox" || inpelems[k].type == "text") {
            frmvalmap.Put(inpelems[k].name, inpelems[k].value);
        }
    }

    divToolbar.innerHTML = "";

    jQuery('div.mmenu').remove();
    jQuery('div').filter(function () {
        return this.className == 'menu-panel' && this.className != 'menu-panel overflow';
    }).remove();
    jQuery('#leftPanelMenu').remove();
    jQuery('div.toolbar-container').html("<div id='divToolbar' class='toolbar-frame'></div>");

    this.init(div);
    inpelems = document.getElementsByTagName("input");
    for (var n = 0; n < inpelems.length; n++) {
        if (inpelems[n].type == "hidden" || inpelems[n].type == "textbox" || inpelems[n].type == "text") {
            inpelems[n].value = frmvalmap.Get(inpelems[n].name);
        }
    }
};

function validateSearchText(strURL, strTarget, strFormFieldName, itemWidth, itemHeight, uiType, submitFunction, submitProgram, isModal, willSubmit, action, sCommandCode) {
    if (isValidSearchTextFTS(jQuery("#AEFGlobalFullTextSearch").val(), parseInt(emxUIConstants.FTS_MINIUM_SEARCHCHARS)))
        submitToolbarForm(strURL, strTarget, strFormFieldName, itemWidth, itemHeight, uiType, submitFunction, submitProgram, isModal, willSubmit, action, sCommandCode);
}

function isValidSearchTextFTS(searchText, minchars) {
    if (searchText.length > 0) {
        var txtVaal = searchText;
        if (searchText.indexOf("*") >= 0 || searchText.indexOf("?") >= 0) {
            while (searchText.indexOf("*") >= 0) {
                searchText = searchText.replace("*", "");
            }
            while (searchText.indexOf("?") >= 0) {
                searchText = searchText.replace("?", "");
            }
            if (searchText.length < minchars) {
                var stralert = emxUIConstants.STR_WILDCHARALERT;
                var strSearchAle = emxUIConstants.STR_WILDCHARCONFIRMSTR;
                var strConfirm = emxUIConstants.STR_WILDCHARCONFIRM;
                strSearchAle = strSearchAle.replace(/{N}/, minchars);
                strSearchAle = strSearchAle.replace(/{ST}/, txtVaal);
                stralert = stralert.replace(/{N}/, minchars);
                stralert = stralert.replace(/{ST}/, txtVaal);
                strConfirm = strConfirm.replace(/{N}/, minchars);
                strConfirm = strConfirm.replace(/{ST}/, txtVaal);
                if (emxUIConstants.FTS_CHAR_VIOLATION == "true") {
                    if (!confirm(strConfirm)) {
                        return false;
                    }
                } else {
                    alert(strSearchAle);
                    return false;
                }
            }
        }
    }
    return true;
}

//! Private Method emxUIToolbar.createDOM()
//!     This method creates the DOM representation of the toolbar.
emxUIToolbar.prototype.createDOM = function _emxUIToolbar_createDOM() {
    if (!this.container) {
        throw new Error("No container element specified. (emxUIToolbar.prototype.createDOM)");
    }
    var isGlobalToolbar = false;
    try {
        isGlobalToolbar = this.container.parentNode.id == "globalToolbar";
    } catch (ex) {
        //do nothing
    }
    if (isGlobalToolbar) {
        return this.createGlobalToolbarDOM();
    }
    var findInCntrlWidth = 0;
    var overflowWidth = 0;
    if (typeof uiType != 'undefined' && uiType == 'structureBrowser') {
        findInCntrlWidth = 32;
        overflowWidth = 21;
    }
    this.maxWidth = this.width == -1 ? -1 : this.width * this.container.offsetWidth - findInCntrlWidth;
    this.element = document.createElement("div");
    this.element.className = "toolbar";
    if (this.container.firstChild && this.container.firstChild.nodeType != 1) {
        this.container.innerHTML = "";
    }

    this.container.appendChild(this.element);
    var objTable = document.createElement("table");
    //objTable.border = 0;
    //objTable.cellPadding = 0;
    //objTable.cellSpacing = 0;
    objTable.height = "100%";
    this.element.appendChild(objTable);
    var objTBody = document.createElement("tbody");
    objTable.appendChild(objTBody);
    var objTR = document.createElement("tr");
    objTBody.appendChild(objTR);
    var overflow_button = null;
    for (var i = 0; i < this.items.length; i++) {
        this.items[i].init(objTR);
        if ((objTable.offsetWidth != 0 && objTable.offsetWidth - findInCntrlWidth - overflowWidth >= this.maxWidth - findInCntrlWidth - overflowWidth && this.maxWidth != -1)
            || this.container.offsetWidth - 40 - findInCntrlWidth < this.element.offsetLeft + this.element.offsetWidth) {

            if (this.items[i].element) {
                if (this.items[i].id != "AEFGenericDelete" && this.items[i].dynamicName != "AEFFullSearchSave" && this.items[i].dynamicName != "AEFFullSearchCollection") {
                    objTR.removeChild(this.items[i].element);
                    this.items[i].element = null;
                    overflow_button = new emxUIToolbarOverflowButton();
                    this.addItem(overflow_button);
                    overflow_button.addMenu(new emxUIToolbarMenu);
                    this.overflow = true;

                    for (var j = i; j < this.items.length - 1; j++) {
                        if (this.items[j].id == "editButttonId") {
                            this.items[j].element = null;
                        }
                        overflow_button.menu.addItem(this.items[j].toMenuItem(), this.items[j].listlink);
                        //To set Mode for items in OverflowButton[Added to support Structure Browser - Edit Mode Toolbar]
                        var item = overflow_button.menu.items[j - i];
                        item.setMode(this.items[j].getMode());
                        item.setDisplayMode(this.items[j].getDisplayMode());
                        if (this.items[j].command == "AEFBackToolbarMenu" || this.items[j].command == "AEFForwardToolbarMenu") {
                            item.enabled = false;
                        }
                    }
                    overflow_button.init(objTR);
                    objTR.appendChild(overflow_button.element);
                    break;
                }
            }
        }
    }
    addEvent(this.element, "contextmenu", cancelEvent);
};
//! Private Method emxUIToolbar.createGlobalToolbarDOM()
//!     This method creates the DOM representation of the global toolbar.
emxUIToolbar.prototype.createGlobalToolbarDOM = function _emxUIToolbar_createGlobalToolbarDOM() {
    if (!this.container) {
        throw new Error(
            "No container element specified. (emxUIToolbar.prototype.createGlobalToolbarDOM)");
    }

    var winwidth = this.container.offsetWidth;
    this.maxWidth = this.width == -1 ? -1 : this.width * this.container.offsetWidth;
    if (this.container.firstChild && this.container.firstChild.nodeType != 1) {
        this.container.innerHTML = "";
    }

    var toolbar_template = function (group) {
        var str = '<div class="toolbar ' + group + '"><ul></ul></div>';
        return jQuery(str).get(0);
    };

    jQuery(this.container)
        .append(toolbar_template("group-left"))
        .append(toolbar_template("group-center"))
        .append(toolbar_template("group-right"));

    var left_width = jQuery('.group-left').width();
    var center_width = jQuery('.group-center').width();
    var right_width = jQuery('.group-right').width();

    this.container_left = jQuery('.group-left').get(0);
    this.container_center = jQuery('.group-center').get(0);
    this.container_right = jQuery('.group-right').get(0);

    var toolbar_section = {
        left: {
            element: this.container_left,
            overflow_button: null,
            table_row: jQuery('ul', this.container_left).get(0),
            maxWidth: left_width
        },
        center: {
            element: this.container_center,
            overflow_button: null,
            table_row: jQuery('ul', this.container_center).get(0),
            maxWidth: center_width
        },
        right: {
            element: this.container_right,
            overflow_button: null,
            table_row: jQuery('ul', this.container_right).get(0),
            maxWidth: right_width
        }
    };

    var cmdname = null, toolbar_alignment = null, toolbar_group = null,
        current_section = null, current_section_row = null,
        itemLength = this.items.length, overflow_icon_width = 20;

    for (var i = 0; i < itemLength; i++) {
        cmdname = this.items[i].text;
        toolbar_alignment = this.items[i].getAlignment();// this item's alignment
        current_section = toolbar_section[toolbar_alignment];// the section object
        toolbar_group = current_section.element;// the TD element from this section
        current_section_row = current_section.table_row;// the row that we're adding items to

        // true passed for global menu
        this.items[i].init(current_section_row, "true");
        if (this.items[i].dynamicName == "My Desk") {
            continue;
        }
        if (this.items[i].dynamicName == "AEFGlobalSearchHolder") {
            this.items[i].menu.dynamicName = "AEFGlobalSearchHolder";
        }

    }
    addEvent(this.container, "contextmenu", cancelEvent);

};
//! Public Method emxUIToolbar.init()
//!     This method draws the actionbar. It should be called in
//!     the page
emxUIToolbar.prototype.init = function _emxUIToolbar_init(strContainerID) {
    if (this.items.length == 0) return;
    this.container = document.getElementById(strContainerID);

    /*
     * This if condition is added for issue with APPImageManager. In
     * emxImageManager.jsp user is adding controls to the objMainTollbar
     * explicitely in the code and passing toolbar parameter. Start
    */
    if (this.name == "context" && !this.container) {
        this.container = document.getElementById("divToolbar");
    }
    //End
    if ((this.name == "context" || this.name == "main") && !this.container) {
        throw new Error("DOM element with ID '" + strContainerID + "' not found. (emxUIToolbar.js::emxUIToolbar.prototype.init)");
    } else if (!this.container) {
        var divCustomToolbarContainer = document.createElement("div");
        divCustomToolbarContainer.setAttribute("id", "Container" + this.name);
        divCustomToolbarContainer.className = "toolbar-container";
        document.getElementById("divToolbarContainer").parentNode.appendChild(divCustomToolbarContainer);

        var customElement = document.createElement("div");
        customElement.setAttribute("id", this.name);
        customElement.className = "toolbar-frame";
        divCustomToolbarContainer.appendChild(customElement);
        this.container = customElement;
    }
    this.createDOM();
};
//! Private Method emxUIToolbar.setListLinks()
//!     This method enables/disables list links.
emxUIToolbar.prototype.setListLinks = function _emxUIToolbar_setListLinks(blnListLink, uiType, SBMode) {
    for (var i = 0; i < this.items.length; i++) {
        var doIgnoreRowSelect = true;
        if (typeof uiType != 'undefined' && uiType == 'structureBrowser' &&
            typeof SBMode != 'undefined' && (this.items[i].Mode != '' && this.items[i].Mode != SBMode)) {
            doIgnoreRowSelect = false;
        }
        if (doIgnoreRowSelect) {
            if (this.items[i].listLink) {
                if (blnListLink && (!this.items[i].grayout || this.items[i].grayout == "" || this.items[i].grayout == "false")) {
                    this.items[i].enable();
                } else {
                    this.items[i].disable();
                }
            }
            if (this.items[i].setListLinks) {
                this.items[i].setListLinks(blnListLink, uiType, SBMode);
            }
        }
    }
};
//! Protected Method emxUIToolbar.setMaxLabelChars()
//!     This method sets the maximum label characters of a button.
emxUIToolbar.prototype.setMaxLabelChars = function _emxUIToolbar_setMaxLabelChars(intLength) {
    this.maxLabelChars = intLength;
};
//! Public Method emxUIToolbar.setWidth()
//!     This method sets the width of the toolbar.
emxUIToolbar.prototype.setWidth = function _emxUIToolbar_setWidth(fltWidth) {
    if (typeof fltWidth == "string") {
        this.width = parseInt(fltWidth) / 100;
    } else {
        this.width = fltWidth;
    }
};
//! Class emxUICoreToolbarItem
//!     This object represents an item on a toolbar. This class is
//!     not intended to be instantiated directly, but rather is used
//!     as a base class for others to extend.
function emxUICoreToolbarItem() {
    this.superclass = emxUIObject;
    this.superclass();
    delete this.superclass;
    this.element = null;
    this.emxClassName = "emxUICoreToolbarItem";
    this.index = -1;
    this.parent = null;
    this.uniqueID = emxUICore.getUniqueID();
    this.listLink = false;
    this.toolbar = null;
    this.grayout = false;
    this.strAlignment = "left";
}

emxUICoreToolbarItem.prototype = new emxUIObject;
//! Private Method emxUICoreToolbarItem.createDOM()
//!     This methods creates the DOM for the item.
emxUICoreToolbarItem.prototype.createDOM = function _emxUICoreToolbarItem_createDOM(objParent) {
    if (objParent.nodeName == "UL") {
        this.element = document.createElement("li");
    } else {
        this.element = document.createElement("td");
    }
    this.element.setAttribute("itemID", this.uniqueID);
    if (this.id) {
        this.element.setAttribute("id", this.id);
    }
    var objThis = this;
    if (this.command == 'AEFCompanyLogo' || this.command == 'AEFCompassHolder' || this.command == 'AEFBrandLogoHolder' || this.command == 'AEFAppLabelHolder') {
        // do nothing
    } else {
        if (this.command == 'AEF6WTagger' && emxUIConstants.STR_QUERY_TYPE != 'Indexed') {
            // do nothing
        } else {
            addEvent(this.element, "click", function () {
                                                                                                    //START IMS
                let button = document.getElementById(objThis.id);
                if (button == null || button.className != 'icon-button-32x32 button-disabled button-disabled') {
                                                                                                    //END IMS
                    objThis.fireEvent("click");
                                                                                                    //START IMS
                }

                if (button != null && button.id.indexOf('Report') > -1) {
                    button.className = 'icon-button-32x32 button-disabled button-disabled';
                    button.disabled = true;
                    console.log("objThis: " + button.className + " " + button.disabled);
                }
                                                                                                    //END IMS
            });
            addEvent(this.element, "mouseover", function () {
                objThis.fireEvent("mouseover");
            });
            addEvent(this.element, "mouseout", function () {
                objThis.fireEvent("mouseout");
            });
            addEvent(this.element, "mouseup", function () {
                objThis.fireEvent("mouseup");
            });
            addEvent(this.element, "mousedown", function () {
                objThis.fireEvent("mousedown");
            });

            this.clearEventHandlers("click");
            this.clearEventHandlers("mouseover");
            this.clearEventHandlers("mouseout");
            this.clearEventHandlers("mouseup");
            this.clearEventHandlers("mousedown");

            this.registerEventHandler("click", function () {
                objThis.handleEvent("click");
            });
            this.registerEventHandler("mouseover", function () {
                objThis.handleEvent("mouseover");
            });
            this.registerEventHandler("mouseout", function () {
                objThis.handleEvent("mouseout");
            });
            this.registerEventHandler("mouseup", function () {
                objThis.handleEvent("mouseup");
            });
            this.registerEventHandler("mousedown", function () {
                objThis.handleEvent("mousedown");
            });
        }
    }
    objParent.appendChild(this.element);
};
//! Private Method emxUICoreToolbarItem.handleEvent()
//!     This method handles the events for this object.
emxUICoreToolbarItem.prototype.handleEvent = function _emxUICoreToolbarItem_handleEvent(strType) {
};
//! Private Method emxUICoreToolbarItem.init()
//!     This method initializes the item.
emxUICoreToolbarItem.prototype.init = function _emxUICoreToolbarItem_init(objParent) {
    this.createDOM(objParent);
};
//! Private Method emxUICoreToolbarItem.reset()
//!     This methods resets the item to its original view.
emxUICoreToolbarItem.prototype.reset = function _emxUICoreToolbarItem_reset() {
};
//! Private Method emxUICoreToolbarItem.getAlignment()
//!     This methods gets the alignment attribute for the item.
emxUICoreToolbarItem.prototype.getAlignment = function _emxUICoreToolbarItem_getAlignment() {
    return this.strAlignment || "left";
};
//! Private Method emxUICoreToolbarItem.toMenuItem()
//!     This method returns a menu item for this toolbar item.
emxUICoreToolbarItem.prototype.toMenuItem = function _emxUICoreToolbarItem_toMenuItem() {
};

//! Class emxUIToolbarButton
function emxUIToolbarButton(intFormat, strIcon, strText, strURL, strTarget,
                            strMenu, strCommand, strCommandTitle, strLinkType,
                            intWidth, intHeight, strSuite, blnHistoryEnabled,
                            intLabelLength, strId, strJPOName, strMethodName, strName, strAlignment) {
    this.superclass = emxUICoreToolbarItem;
    this.superclass();
    delete this.superclass;
    this.cssClass = "";
    this.emxClassName = "emxUIToolbarButton";
    this.format = intFormat;
    this.icon = strIcon == null ? null : emxUICore.getIcon(strIcon);
    this.menu = null;
    this.menuDir = "down";
    this.target = strTarget;
    this.text = strText;
    this.url = strURL;
    this.historyEnabled = blnHistoryEnabled;
    this.linkMenu = strMenu;
    this.command = strCommand;
    this.commandTitle = strCommandTitle;
    this.linkType = strLinkType;
    this.linkWidth = intWidth;
    this.linkHeight = intHeight;
    this.suite = strSuite;
    this.labelLength = intLabelLength;
    this.id = strId;
    this.dynamicJPO = strJPOName;
    this.dynamicMethod = strMethodName;
    this.dynamicName = strName;
    this.strAlignment = strAlignment;
}

emxUIToolbarButton.prototype = new emxUICoreToolbarItem;
//! Private Method emxUIToolbarButton.addMenu()
//!     This method assigns a menu to the toolbar.
emxUIToolbarButton.prototype.addMenu = function _emxUIToolbarButton_addMenu(objMenu) {
    if (!this.parent) {
        throw new Error("Toolbar button must be added to a toolbar before a menu can be added. (emxUIToolbar.js::emxUIToolbarButton.prototype.addMenu)");
    }
    this.menu = objMenu;
    objMenu.expanded = this.expanded;
    objMenu.displayMode = this.displayMode;

    //For Commands and Menus directly on toolbar and Dynamic property to the associated menu
    if (this.dynamicJPO != "undefined" && this.dynamicJPO != null) {
        objMenu.dynamicJPO = this.dynamicJPO;
        objMenu.dynamicMethod = this.dynamicMethod;
        objMenu.dynamicName = this.dynamicName;
    }
    objMenu.historyEnabled = this.parent.historyEnabled;
    if (this.parent.mode == emxUIToolbar.MODE_FRAMES) {
        var blnDone = false;
        for (var i = 0; i < parent.frames.length && !blnDone; i++) {
            if (parent.frames[i] == self) {
                this.menu.displayWindow = parent.frames[i + 1];
                blnDone = true;
            }
        }
    }
};
//! Private Method emxUIToolbarButton.createDOM()
//!     This methods creates the DOM for the item.
emxUIToolbarButton.prototype.emxUIToolbarItemCreateDOM = emxUIToolbarButton.prototype.createDOM;
emxUIToolbarButton.prototype.createDOM = function _emxUIToolbarButton_createDOM(objParent) {
    var strDisplayText = this.text.htmlDecode();

    //labelLength comes from the command setting and has precedence over maxLabelChars
    if (this.labelLength != null && this.labelLength > 0) {
        if (strDisplayText.length > this.labelLength) {
            strDisplayText = strDisplayText.substring(0, Math.min(this.labelLength, strDisplayText.length)) + "...";
        }
    } else {
        if (this.parent.maxLabelChars > -1 && strDisplayText.length > this.parent.maxLabelChars) {
            strDisplayText = strDisplayText.substring(0, Math.min(this.parent.maxLabelChars, strDisplayText.length)) + "...";
        }
    }
    strDisplayText = strDisplayText.htmlEncode();
    var distext = strDisplayText;
    //combination button
    if (this.url && this.menu) {
        if (objParent.nodeName == "UL") {
            this.buttonElement = document.createElement("li");
            this.menuElement = document.createElement("li");
        } else {
            this.buttonElement = document.createElement("td");
            this.menuElement = document.createElement("td");
        }
        this.buttonElement.title = this.menuElement.title = this.text.htmlDecode();
        this.menuElement.innerHTML = "<img data-type=\"combination\" src=\"" + emxUIToolbar.IMG_MENU_ARROW + "\" border=\"0\" />";
        this.menuElement.className = "menu-arrow";

        if (this.icon != null && this.icon != 'undefined' && this.icon != '' && this.icon != emxUIConstants.DIR_SMALL_ICONS) {
            this.format = emxUIToolbar.ICON_ONLY;
        }

        switch (this.format) {
            case emxUIToolbar.ICON_AND_TEXT:
                this.buttonElement.className = "icon-and-text-button combo-button";
                this.buttonElement.innerHTML = "<img src=\"" + this.icon + "\"/>" + distext;
                break;
            case emxUIToolbar.TEXT_ONLY:
                this.buttonElement.className = "text-button combo-button";
                this.buttonElement.innerHTML = strDisplayText;
                break;
            case emxUIToolbar.ICON_ONLY:
                if (this.icon.indexOf('.gif', this.icon.length - 4) !== -1) {
                    this.buttonElement.className = "icon-button-16x16 combo-button";
                } else {
                    this.buttonElement.className = "icon-button combo-button";
                }

                //this.buttonElement.innerHTML = "&nbsp;";
                this.buttonElement.innerHTML = "<img src=\"" + this.icon + "\"/>";
                break;
        }
        if (emxUIConstants.UI_AUTOMATION == 'true') {
            this.buttonElement.setAttribute('data-aid', this.buttonElement.title);
            this.menuElement.setAttribute('data-aid', 'menu-arrow');
        }
        var objThis = this;
        addEvent(this.buttonElement, "click", function () {
            objThis.fireEvent("button-click");
        });
        addEvent(this.buttonElement, "mouseover", function () {
            objThis.fireEvent("mouseover");
        });
        addEvent(this.buttonElement, "mouseout", function () {
            objThis.fireEvent("mouseout");
        });
        addEvent(this.buttonElement, "mouseup", function () {
            objThis.fireEvent("button-mouseup");
        });
        addEvent(this.buttonElement, "mousedown", function () {
            objThis.fireEvent("button-mousedown");
        });

        addEvent(this.menuElement, "click", function () {
            objThis.fireEvent("menu-click");
        });
        addEvent(this.menuElement, "mouseover", function () {
            objThis.fireEvent("mouseover");
        });
        addEvent(this.menuElement, "mouseout", function () {
            objThis.fireEvent("mouseout");
        });
        addEvent(this.menuElement, "mouseup", function () {
            objThis.fireEvent("menu-mouseup");
        });
        addEvent(this.menuElement, "mousedown", function () {
            objThis.fireEvent("menu-mousedown");
        });

        this.clearEventHandlers("button-click");
        this.clearEventHandlers("button-mouseup");
        this.clearEventHandlers("button-mousedown");
        this.clearEventHandlers("menu-click");
        this.clearEventHandlers("mouseover");
        this.clearEventHandlers("mouseout");
        this.clearEventHandlers("menu-mouseup");
        this.clearEventHandlers("menu-mousedown");

        this.registerEventHandler("button-click", function () {
            objThis.handleEvent("button-click");
        });
        this.registerEventHandler("button-mouseup", function () {
            objThis.handleEvent("button-mouseup");
        });
        this.registerEventHandler("button-mousedown", function () {
            objThis.handleEvent("button-mousedown");
        });
        this.registerEventHandler("menu-click", function () {
            objThis.handleEvent("menu-click");
        });
        this.registerEventHandler("mouseover", function () {
            objThis.handleEvent("mouseover");
        });
        this.registerEventHandler("mouseout", function () {
            objThis.handleEvent("mouseout");
        });
        this.registerEventHandler("menu-mouseup", function () {
            objThis.handleEvent("menu-mouseup");
        });
        this.registerEventHandler("menu-mousedown", function () {
            objThis.handleEvent("menu-mousedown");
        });
        objParent.appendChild(this.buttonElement);
        objParent.appendChild(this.menuElement);
    } else {
        this.emxUIToolbarItemCreateDOM(objParent);
        this.element.title = this.text.htmlDecode();
        this.element.noWrap = "nowrap";
        if (emxUIConstants.UI_AUTOMATION == 'true') {
            if (this.command) {
                this.element.setAttribute('data-aid', this.command);
            } else if (this.dynamicName) {
                this.element.setAttribute('data-aid', this.dynamicName);
            } else {
                this.element.setAttribute('data-aid', this.text);
            }
        }
        var isGlobalToolbar = false;
        try {
            isGlobalToolbar = objParent.parentNode.parentNode.parentNode.id == "globalToolbar";
        } catch (ex) {
            //do nothing
        }
        if (this.icon != null && this.icon != 'undefined' && this.icon != '' && this.icon != emxUIConstants.DIR_SMALL_ICONS) {
            if (this.icon.indexOf('iconGenericBlue') >= 0) {
                this.format = emxUIToolbar.TEXT_ONLY;
            } else {
                this.format = emxUIToolbar.ICON_ONLY;
            }
        }


        switch (this.format) {
            case emxUIToolbar.ICON_AND_TEXT:
                var w = 16;
                var cName = "icon-and-text-button";
                if (this.icon.indexOf("3dsButton") != -1) {
                    w = 24;
                    strDisplayText = "";
                    cName += " icon-and-text-button-ds-logo";
                }
                this.element.className = cName;
                if (this.menu && !isGlobalToolbar) {
                    this.element.innerHTML = "<img src=\"" + this.icon + "\"/>" + distext + "&nbsp;<img src=\"" + emxUIToolbar.IMG_MENU_ARROW + "\"/>";
                } else {
                    this.element.innerHTML = "<img src=\"" + this.icon + "\"/>" + distext;
                }
                break;
            case emxUIToolbar.TEXT_ONLY:
                if (this.url || this.menu) {
                    this.element.className = "text-button";
                } else {
                    this.element.id = this.command;
                }
                if (this.menu && !isGlobalToolbar) {
                    this.element.innerHTML = strDisplayText + "&nbsp;<img src=\"" + emxUIToolbar.IMG_MENU_ARROW + "\"/>";
                } else {
                    this.element.innerHTML = strDisplayText;
                }
                break;
            case emxUIToolbar.ICON_ONLY:
                if (this.url || this.menu) {
                    if (this.command == 'AEFPersonMenu') {
                        this.element.className = "icon-button profile";
                    } else if (this.command == 'Actions') {
                        this.element.className = "icon-button add";
                    } else if (this.command == 'AEFMyHome' || this.command == "AEFHomeToolbar") {
                        this.element.className = "icon-button home";
                    } else if (this.command == 'AEFShareMenu') {
                        this.element.className = "icon-button share";
                    } else if (this.command == 'AEFHelpMenu') {
                        this.element.className = "icon-button help";
                    } else {
                        if (this.icon.indexOf('.gif', this.icon.length - 4) !== -1) {
                            this.element.className = "icon-button-16x16";
                        } else {
                            this.element.className = "icon-button";
                        }


                    }
                } else {
                    this.element.id = this.command;
                }
                //this.element.innerHTML = "&nbsp;";
                if (this.menu && !isGlobalToolbar) {
                    if (this.icon.indexOf('.gif', this.icon.length - 4) !== -1) {
                        this.element.className = "icon-button-16x16 menu-button-16x16";
                    } else {
                        this.element.className = "icon-button menu-button";
                    }
                    this.element.innerHTML = "<img src=\"" + this.icon + "\"/><img src=\"" + emxUIToolbar.IMG_MENU_ARROW + "\"/>";
                } else {
                    if (this.command == 'AEFCompanyLogo' || this.command == 'AEFPersonMenu' || this.command == 'Actions' || this.command == 'AEFMyHome' || this.command == "AEFHomeToolbar" || this.command == 'AEFShareMenu' || this.command == 'AEFShareToolbar') {
                        this.element.innerHTML = "<span></span>";
                    } else {
                        this.element.innerHTML = "<img data-type=\"command\" src=\"" + this.icon + "\"/>";
                    }
                }
                break;
        }

        if (this.command == 'AEFHelpMenu') {
            this.element.className = "icon-button help";
            this.element.innerHTML = "<span></span>";
        } else if (this.command == 'AEFShareToolbar') {
            this.element.className = "icon-button share";
            this.element.innerHTML = "<span></span>";
        }

        if (this.command == 'AEFCompassHolder') {
            this.element.className = "compass-placeholder";
            this.element.innerHTML = "<span></span>";
        }
        if (this.command == 'AEFBrandLogoHolder') {
            this.element.innerHTML = "<span>" + strDisplayText + "</span>";
        }
        if (this.command == 'AEFAppLabelHolder') {
            this.element.innerHTML = '&nbsp;<span></span>';
        }

        if (this.command == 'AEFGlobalSearchHolder') {
            var innDiv = jQuery('<div class="search" id="GTBsearchDiv"></div>');
            var innDiv1 = jQuery('<div class="search-container"></div>');
            var innDiv4 = jQuery(' <div class="search-context" id="AEFGlobalFullTextSearch" typeName="All_Search"><a href="#"><label>' + emxUIConstants.SEARCH_ALL + '</label></a></div>');
            var innDiv2 = jQuery('<div class="search-input" onclick="hideMenu(event)"><input type="text" autocomplete="on" id="GlobalNewTEXT" onkeyup="searchTextBox(event)" onkeypress="isSearchEnterKeyPressed(event)" onclick="textboxClicked(this)" onblur="textboxOnBlur(this)"  value="' + emxUIConstants.SEARCH + '"/></div>');
            var innDiv3 = jQuery('<div class="search-actions"><ul><li  style="display:none" id="searchClear"><a href="javascript:clearSearch()" class="btn clear" title= "' + emxUIConstants.STR_SEARCH_CLEAR + '"></a></li>  <li><a href="javascript:isSearchEnterKeyPressed()" class="btn search"></a></li> </ul></div>');
            innDiv.append(innDiv1);
            innDiv1.append(innDiv4);
            innDiv1.append(innDiv2);
            innDiv1.append(innDiv3);
            this.element.className = "search-widget";
            this.element.innerHTML = jQuery('<div/>').append(innDiv).html();
        }
        if (this.command == 'AEF6WTagger') {
            this.element.innerHTML = "<a ></a>";
            if (emxUIConstants.STR_QUERY_TYPE == 'Indexed') {
                this.element.className = "tagger";
                this.element.onclick = function () {
                    toggleTagger();
                };
            } else {
                this.element.className = "tagger button-disabled";
                this.element.title = emxUIConstants.SixWTagging_EXALEAD_MESSAGE;
                this.element.onclick = function () {
                    return;
                };
            }
        }

    }
    if (this.grayout == "true") {
        this.disable();
    }
};
//! Private Method emxUIToolbarButton.handleEvent()
//!     This method handles the events for this object.
emxUIToolbarButton.prototype.handleEvent = function _emxUIToolbarButton_handleEvent(strType) {
    if (this.menu && this.url) {
        switch (strType) {
            case "mouseover":
                emxUICore.addClass(this.buttonElement, "button-hover");
                emxUICore.addClass(this.menuElement, "button-hover");
                break;
            case "mouseout":
                if (!this.menu.visible) {
                    this.reset();
                }
                break;
            case "button-click":
                emxUICore.removeClass(this.buttonElement, "button-hover");
                emxUICore.removeClass(this.menuElement, "button-hover");
                emxUICore.addClass(this.buttonElement, "button-active");
                emxUICore.addClass(this.menuElement, "button-active");
                if (this.parent.historyEnabled && this.historyEnabled) {
                    emxUICore.addToPageHistory(this.suite, this.url, this.linkMenu, this.command, this.target, this.commandTitle, this.linkType, this.linkWidth, this.linkHeight);
                }
                emxUICore.link(this.url, this.target);
                emxUICore.removeClass(this.buttonElement, "button-active");
                emxUICore.removeClass(this.menuElement, "button-active");
                break;
            case "button-mousedown":
                emxUICore.removeClass(this.buttonElement, "button-hover");
                emxUICore.addClass(this.buttonElement, "button-active");
                emxUICore.removeClass(this.menuElement, "button-hover");
                emxUICore.addClass(this.menuElement, "button-active");
                break;
            case "button-mouseup":
                emxUICore.removeClass(this.buttonElement, "button-active");
                emxUICore.removeClass(this.menuElement, "button-active");
                break;
            case "menu-click":
                emxUICore.removeClass(this.menuElement, "button-hover");
                emxUICore.addClass(this.menuElement, "button-active");
                this.menu.show(this.buttonElement, this.menuDir);
                break;
        }
    } else if (this.menu) {
        switch (strType) {
            case "mouseover":
                var timeout = Browser.MOBILE ? 1000 : 0;
                if (this.command != 'AEFGlobalSearchHolder') {
                    if (!this.menu.visible) {
                        emxUICore.addClass(this.element, "button-hover");
                    }
                }
                if (this.command == 'AEFPersonMenu' || this.command == 'Actions' || this.command == 'AEFMyHome' || this.command == 'AEFShareMenu' || this.command == 'AEFHelpMenu') {
                    jQuery(".menu-panel:visible").hide();
                    jQuery('#ieMenuCoverForObjectTag').remove();
                    emxUICore.removeClass(jQuery('li.add')[0], 'button-hover');
                    emxUICore.removeClass(jQuery('li.profile')[0], 'button-hover');
                    emxUICore.removeClass(jQuery('li.share')[0], 'button-hover');
                    emxUICore.removeClass(jQuery('li.home')[0], 'button-hover');
                    emxUICore.removeClass(jQuery('li.help')[0], 'button-hover');
                    this.menu.show(this.element, this.menuDir);
                    setTimeout(function () {
                        jQuery(".menu-panel:visible").bind("mouseout", function (e) {
                            var reltg = (e.relatedTarget) ? e.relatedTarget : e.toElement;
                            if ((getTopWindow().isMobile || getTopWindow().isPCTouch) && reltg == null) {
                                reltg = e.target;
                            }
                            while (reltg && !jQuery(reltg).hasClass('menu-panel') && reltg.nodeName != 'BODY') {
                                reltg = reltg.parentNode;
                            }
                            if (!jQuery(reltg).hasClass('menu-panel')) {
                                jQuery(this).unbind("mouseout");
                                jQuery(this).hide();
                                jQuery('#ieMenuCoverForObjectTag').remove();
                            }
                        });
                    }, timeout);
                }

                break;
            case "mouseout":
                if (this.command != 'AEFGlobalSearchHolder') {
                    if (!this.menu.visible) {
                        this.reset();
                    }
                }
                break;
            case "click":
                if (this.command != 'AEFGlobalSearchHolder') {
                    emxUICore.removeClass(this.element, "button-hover");
                    emxUICore.addClass(this.element, "menu-button-active");
                }

                if (this.command == 'AEFPersonMenu' || this.command == 'Actions' || this.command == 'AEFMyHome' || this.command == 'AEFShareMenu' || this.command == 'AEFHelpMenu') {
                    this.menu.show(this.element, this.menuDir);
                } else {
                    this.menu.show(this.element, this.menuDir, this);
                }
        }
    } else {
        switch (strType) {
            case "mouseover":
                if (this.command != 'AEF6WTagger') {
                    emxUICore.addClass(this.element, "button-hover");
                }
                break;
            case "mouseout":
                this.reset();
                break;
            case "click":
                if (this.command == "AEFHomeToolbar") {
                    getTopWindow().bclist.clear();
                }


                if (this.url.indexOf("javascript:showPageURL(") < 0) {
                    if (this.parent.historyEnabled && this.historyEnabled) {
                        emxUICore.addToPageHistory(this.suite, this.url, this.linkMenu, this.command, this.target, this.commandTitle, this.linkType, this.linkWidth, this.linkHeight);
                    }
                    emxUICore.link(this.url, this.target, this.emxClassName);
                } else {
                    var objThis = this;
                    showPageURL(objThis);
                }

                break;
            case "mousedown":
                if (this.command != 'AEF6WTagger') {
                    emxUICore.removeClass(this.element, "button-hover");
                    emxUICore.addClass(this.element, "button-active");
                }
                break;
            case "mouseup":
                if (this.command != 'AEF6WTagger') {
                    emxUICore.removeClass(this.element, "button-active");
                }
                break;
        }
    }
};
//! Private Method emxUIToolbarButton.init()
//!     This method initializes the item.
emxUIToolbarButton.prototype.init = function _emxUIToolbarButton_init(objParent, gtb) {
    if (this.command != "My Desk" && (!emxUIConstants.TOPFRAME_ENABLED || gtb != "true")) {
        this.createDOM(objParent);
    }
    if (this.id === "Categories") {
        this.menu.id = "Categories";
    }
    if (this.menu && (!emxUIConstants.TOPFRAME_ENABLED || (!gtb || this.command == "My Desk"))) {
        this.menu.init("", gtb);
        var objThis = this;
        this.menu.clearEventHandlers("hide");
        this.menu.registerEventHandler("hide", function () {
            objThis.reset();
        });
    }
};
//! Private Method emxUIToolbarButton.disable()
//!     This method disables the button.
emxUIToolbarButton.prototype.emxUICoreToolbarItemDisable = emxUIToolbarButton.prototype.disable;
emxUIToolbarButton.prototype.disable = function _emxUIToolbarButton_disable() {
    this.emxUICoreToolbarItemDisable();
    if (this.element) {
        if (this.url && this.menu) {
            emxUICore.addClass(this.buttonElement, "button-disabled");
            emxUICore.addClass(this.menuElement, "button-disabled");
        } else {
            emxUICore.addClass(this.element, "button-disabled");
        }
    }
    //for graying Menu with href when it is disabled[Added to support Structure Browser - Edit Mode Toolbar]
    if (this.buttonElement) {
        emxUICore.addClass(this.buttonElement, "button-disabled");
    }
};
//! Private Method emxUIToolbarButton.enable()
//!     This method enables the button.
emxUIToolbarButton.prototype.emxUICoreToolbarItemEnable = emxUIToolbarButton.prototype.enable;
emxUIToolbarButton.prototype.enable = function _emxUIToolbarButton_enable() {
    this.emxUICoreToolbarItemEnable();
    if (this.element) {
        if (this.menu && this.url) {
            emxUICore.removeClass(this.buttonElement, "button-disabled");
            emxUICore.removeClass(this.menuElement, "button-disabled");
        } else {
            emxUICore.removeClass(this.element, "button-disabled");
        }
    }
    //To Undo graying Menu with href when it is enabled[Added to support Structure Browser - Edit Mode Toolbar]
    if (this.buttonElement) {
        emxUICore.removeClass(this.buttonElement, "button-disabled");
    }
};
//! Private Method emxUIToolbarButton.reset()
//!     This methods resets the item to its original view.
emxUIToolbarButton.prototype.reset = function _emxUIToolbarButton_reset() {
    if (this.menu && this.url) {
        emxUICore.removeClass(this.buttonElement, "button-hover");
        emxUICore.removeClass(this.buttonElement, "button-active");
        emxUICore.removeClass(this.menuElement, "button-hover");
        emxUICore.removeClass(this.menuElement, "button-active");
    } //modified for bug : 345627
    else if (this.element != null) {
        emxUICore.removeClass(this.element, "button-hover");
        emxUICore.removeClass(this.element, "button-active");
        emxUICore.removeClass(this.element, "menu-button-active");
    }
    if (this.menu && this.menu.visible) {
        this.menu.hide();
    }
};
//! Private Method emxUIToolbarButton.setListLinks()
//!     This method enables/disables list links.
emxUIToolbarButton.prototype.setListLinks = function _emxUIToolbarButton_setListLinks(blnListLink, uiType, SBMode) {
    if (this.menu) {
        this.menu.setListLinks(blnListLink, uiType, SBMode);
    }
};
//! Private Method emxUIToolbarButton.toMenuItem()
//!     This method returns a menu item for this toolbar item.
emxUIToolbarButton.prototype.toMenuItem = function _emxUIToolbarButton_toMenuItem() {
    var isHTMLControl = this.hasOwnProperty("htmlControl");
    var htmlType = (isHTMLControl) ? this.htmlControl : '';
    var fieldName = (this.formFieldName) ? this.formFieldName : '';
    var objItem = new emxUIToolbarMenuItem(emxUIToolbar.ICON_AND_TEXT, this.icon, this.text, this.url, this.target, "", this.command, "", "", "", "", "", "", "", "", "", isHTMLControl, htmlType, fieldName, this.grayout);
    if (this.menu) {
        objItem.addMenu(this.menu);
    }
    return objItem;
};
//! Private Class emxUIToolbarOverflowButton
//!     This object should not be instantiated directly.
function emxUIToolbarOverflowButton() {
    this.superclass = emxUIToolbarButton;
    this.superclass(emxUIToolbar.ICON_ONLY, "", "");
    delete this.superclass;
    this.emxClassName = "emxUIToolbarOverflowButton";
    this.menuDir = "down-left";
    this.command = "emxUIToolbarOverflowButton";
    this.url = "";
}

emxUIToolbarOverflowButton.prototype = new emxUIToolbarButton;
//! Private Method emxUIToolbarOverflowButton.createDOM()
//!     This methods creates the DOM for the item.
//!     (none)
//!     The DOM element representing this toolbar item.
emxUIToolbarOverflowButton.prototype.createDOM = function _emxUIToolbarOverflowButton_createDOM(objParent) {
    this.emxUIToolbarItemCreateDOM(objParent);
    this.element.noWrap = "nowrap";
    this.element.className = "overflow-button";
    if (emxUIConstants.UI_AUTOMATION == 'true') {
        if (this.command) {
            this.element.setAttribute('data-aid', this.command);
        } else if (this.dynamicName) {
            this.element.setAttribute('data-aid', this.dynamicName);
        } else {
            this.element.setAttribute('data-aid', this.text);
        }
    }
    this.element.innerHTML = "<img src=\"" + emxUIToolbar.IMG_CHEVRON + "\" alt=\"\">";
};

//! Class emxUIToolbarSeparator
function emxUIToolbarSeparator() {
    this.superclass = emxUICoreToolbarItem;
    this.superclass();
    delete this.superclass;
    this.emxClassName = "emxUIToolbarSeparator";
}

emxUIToolbarSeparator.prototype = new emxUICoreToolbarItem;
//! Private Method emxUIToolbarSeparator.createDOM()
//!     This methods creates the DOM for the item.
//!     (none)
//!     The DOM element representing this menu item.
emxUIToolbarSeparator.prototype.emxUIToolbarItemCreateDOM = emxUIToolbarSeparator.prototype.createDOM;
emxUIToolbarSeparator.prototype.createDOM = function _emxUIToolbarSeparator_createDOM(objParent) {
    this.emxUIToolbarItemCreateDOM(objParent);
    this.element.innerHTML = "<div class=\"separator\"></div>";
};
//! Private Method emxUIToolbarSeparator.toMenuItem()
//!     This method returns a menu item for this toolbar item.
emxUIToolbarSeparator.prototype.toMenuItem = function _emxUIToolbarSeparator_toMenuItem() {
    return new emxUIToolbarMenuSeparator();
};
//! Class emxUIToolbarMenu
//!     This object represents a menu.
function emxUIToolbarMenu() {
    this.superclass = emxUICorePopupMenu;
    this.superclass();
    delete this.superclass;
    this.toolbar = null;
    this.emxClassName = "emxUIToolbarMenu";
}

emxUIToolbarMenu.prototype = new emxUICorePopupMenu;
//! Protected Method emxUIToolbarMenu.addItem()
//!     This method adds an item to the menu.
emxUIToolbarMenu.prototype.emxUICorePopupMenuAddItem = emxUIToolbarMenu.prototype.addItem;
emxUIToolbarMenu.prototype.addItem = function _emxUIToolbarMenu_addItem(objItem, blnListLink) {
    objItem.listLink = !!blnListLink;
    if (objItem.text == emxUIConstants.STR_LOADING_MENU) {
        //If a Sub Menu Item is dynamic,Dynamic setting is added to the parent Menu
        if (this.parent != null) {
            this.parent.menu.dynamicJPO = objItem.dynamicJPO;
            this.parent.menu.dynamicMethod = objItem.dynamicMethod;
            this.parent.menu.dynamicName = objItem.dynamicName;
        } else {
            this.dynamicJPO = objItem.dynamicJPO;
            this.dynamicMethod = objItem.dynamicMethod;
            this.dynamicName = objItem.dynamicName;
        }
    }
    return this.emxUICorePopupMenuAddItem(objItem);
};
//! Private Method emxUIToolbarMenu.setListLinks()
//!     This method enables/disables list links.
emxUIToolbarMenu.prototype.setListLinks = function _emxUIToolbarMenu_setListLinks(blnListLink, uiType, SBMode) {
    for (var i = 0; i < this.items.length; i++) {
        var doIgnoreRowSelect = true;
        if (typeof uiType != 'undefined' && uiType == 'structureBrowser' &&
            typeof SBMode != 'undefined' && (this.items[i].Mode != '' && this.items[i].Mode != SBMode)) {
            doIgnoreRowSelect = false;
        }
        if (doIgnoreRowSelect) {
            if (this.items[i].listLink) {
                if (blnListLink && (!this.items[i].grayout || this.items[i].grayout == "" || this.items[i].grayout == "false")) {
                    this.items[i].enable();
                } else {
                    this.items[i].disable();
                }
            }
            if (this.items[i].menu) {
                this.items[i].menu.setListLinks(blnListLink, uiType, SBMode);
            }
        }
    }
};
//! Class emxUIToolbarMenuSeparator
//!     This object represents a menu separator.
function emxUIToolbarMenuSeparator() {
    this.superclass = emxUICoreMenuSeparator;
    this.superclass();
    delete this.superclass;
    this.emxClassName = "emxUIToolbarMenuSeparator";
}

emxUIToolbarMenuSeparator.prototype = new emxUICoreMenuSeparator;

//! Class emxUIToolbarMenuItem
function emxUIToolbarMenuItem(intFormat, strIcon, strText, strURL, strTarget,
                              strMenu, strCommand, strCommandTitle, strLinkType,
                              intWidth, intHeight, strSuite, blnHistoryEnabled, strJPOName, strMethodName, strName,
                              isHTMLControl, htmlType, fieldName, grayout) {
    this.superclass = emxUICoreMenuLink;
    this.superclass((intFormat == emxUIToolbar.ICON_AND_TEXT ? strIcon : null), strText, strURL, strTarget);
    delete this.superclass;
    this.emxClassName = "emxUIToolbarMenuItem";
    this.format = intFormat;
    this.icon = ((strIcon == null) || (strIcon == "")) ? null : emxUICore.getIcon(strIcon);
    this.linkMenu = strMenu;
    this.command = strCommand;
    this.commandTitle = strCommandTitle;
    this.linkType = strLinkType;
    this.linkWidth = intWidth;
    this.linkHeight = intHeight;
    this.suite = strSuite;
    this.historyEnabled = blnHistoryEnabled;
    this.dynamicJPO = strJPOName;
    this.dynamicMethod = strMethodName;
    this.dynamicName = strName;
    this.isHTMLControl = isHTMLControl;
    this.htmlType = htmlType;
    this.fieldName = fieldName;
    this.grayout = grayout;
}

emxUIToolbarMenuItem.prototype = new emxUICoreMenuLink;

emxUIToolbarMenuItem.prototype.addMenu = function _emxUIToolbarMenuItem_addMenu(objMenu) {
    this.menu = objMenu;
    //If the Toolbar Item is Dynamic add Dynamic Property to the associated menu
    if (this.dynamicJPO != "undefined" && this.dynamicJPO != null && this.dynamicJPO != "") {
        objMenu.dynamicJPO = this.dynamicJPO;
        objMenu.dynamicMethod = this.dynamicMethod;
        objMenu.dynamicName = this.dynamicName;
    }
    this.menu.parent = this;
    if (this.parent != null) {
        this.menu.historyEnabled = this.parent.historyEnabled;
    }
};

emxUIToolbarMenuItem.prototype.click = function _emxUIToolbarMenuItem_click() {
    try {
        if (this.onClick != null) {
            eval(this.onClick);
        }
    } catch (e) {

    }

    if (!this.dead && !this.isHTMLControl) {
        this.parent.hide(true);
        if (this.parent.historyEnabled && this.historyEnabled) {
            emxUICore.addToPageHistory(this.suite, this.url, this.linkMenu, this.command, this.target, this.commandTitle, this.linkType, this.linkWidth, this.linkHeight);
        }

        //added for bug : 342600
        if (this.isRMB == "true") {
            if (this.url != "" && this.target == "content") {
                this.target = "popup";

                if (this.url.indexOf("javascript:") != 0) {
                    this.url = "javascript:showModalDialog(\"" + this.url + "\",600,600,true )";
                }
            }
        }
    }
};

//! Class emxUIToolbarMenuTitle
function emxUIToolbarMenuTitle(strText) {
    this.superclass = emxUICoreMenuTitle;
    this.superclass(strText);
    delete this.superclass;
    this.emxClassName = "emxUIToolbarMenuTitle";
    this.text = strText;
}

emxUIToolbarMenuTitle.prototype = new emxUICoreMenuTitle;

//This function iterates through html elements in overflow menu
function appendOverflowMenuFormElements(form) {
    overflowMenuFormFieldValues = '';
    for (var i = 0; i < toolbars.length; i++) {
        for (var j = 0; j < toolbars[i].items.length; j++) {
            if (toolbars[i].items[j].emxClassName == 'emxUIToolbarOverflowButton') {
                for (var k = 0; k < toolbars[i].items[j].menu.items.length; k++) {
                    var overflownItem = toolbars[i].items[j].menu.items[k];
                    if (overflownItem.isHTMLControl) {
                        appendToFormFieldValues(overflownItem);
                    }
                }
            }
        }
    }
}

var overflowMenuFormFieldValues = '';

//This function appends html elements in overflow menu to overflowMenuFormFieldValues
function appendToFormFieldValues(overFlownHTMLElement) {
    var objInput = new Array();
    switch (overFlownHTMLElement.htmlType) {
        case "textbox":
        case "text":
        case "checkbox":
            objInput = overFlownHTMLElement.rowElement.getElementsByTagName("INPUT");
            break;
        case "combobox":
            objInput = overFlownHTMLElement.rowElement.getElementsByTagName("SELECT");
            break;
        default:
    }
    for (var n = 0; n < objInput.length; n++) {
        if (typeof getInputValue(objInput[n]) != "undefined") {
            overflowMenuFormFieldValues += "&" + objInput[n].name + "=" + getInputValue(objInput[n]);
        }
    }
}

//This function returns field values of html controls in overflow menu
function getInputValue(obj) {
    switch (obj.type) {
        case "textarea":
        case "text":
        case "hidden":
            return obj.value;
            break;
        case "select-one":
            return obj[obj.selectedIndex].value;
            break;
        case "checkbox":
            return obj.checked;
            break;
        default:
    }
}

//This function returns form fields for items in overflow menu
function getFieldFromOverflowmenu(fieldName, type) {
    var reqField = [];
    for (var i = 0; i < toolbars.length; i++) {
        for (var j = 0; j < toolbars[i].items.length; j++) {
            if (toolbars[i].items[j].emxClassName == 'emxUIToolbarOverflowButton') {
                for (var k = 0; k < toolbars[i].items[j].menu.items.length; k++) {
                    var overflownItem = toolbars[i].items[j].menu.items[k];
                    if (overflownItem.fieldName == fieldName) {
                        reqField[0] = jQuery("input[name=" + fieldName + "]")[0];
                        //will be called from emxUIPopupCalendar.prototype.assignInputs in emxUICalendar.js
                        //type is the fields type based on which the hidden fields value will be returned.
                        if (type == "date") {
                            reqField[1] = jQuery("input[name=" + fieldName + "_msvalue]")[0];
                        }
                        break;
                    }
                }
            }
        }
    }
    return reqField;
}

// Added for Toolbar enhancement code - Begin
//This method submits the toolbar html controls to the specified target location.
function submitToolbarForm(strURL, strTarget, strFormFieldName, itemWidth, itemHeight, uiType, submitFunction, submitProgram, isModal, willSubmit, formField, sCommandCode, slideInInputData, resetFilterIcon) {
    if (resetFilterIcon) {
        getTopWindow().filterApplied = true;
    }

    var tempCode = eval(sCommandCode);
    if (tempCode) {
        tempCode.main();
        return;
    }
    if (strURL == null || strURL == "") {
        return false;
    }
    var formCount = 0;
    var formFieldValues = slideInInputData;
    var fieldValue = "";
    var checkBoxValues = {};
    var checkBoxName = "";
    appendOverflowMenuFormElements(document.forms[formCount]);
    for (var i = 0; i < document.forms[formCount].elements.length; i++) {
        if (document.forms[formCount].elements[i].type == "text") {
            fieldValue = document.forms[formCount].elements[i].value;
            formFieldValues += "&" + document.forms[formCount].elements[i].name + "=" + fieldValue;
        } else if (document.forms[formCount].elements[i].type == "select-one" && document.forms[formCount].elements[i].selectedIndex >= 0) {
            //Modified to pass the selected option value instead of display value.
            //this will fail in internationalization if value is not passed
            fieldValue = document.forms[formCount].elements[i].options[document.forms[formCount].elements[i].selectedIndex].value;
            formFieldValues += "&" + document.forms[formCount].elements[i].name + "=" + fieldValue;
        }
        // Added for Toolbar HTML Widgets to pass the hidden msValue for Date
        else if (document.forms[formCount].elements[i].type == "hidden") {
            fieldValue = document.forms[formCount].elements[i].value;
            formFieldValues += "&" + document.forms[formCount].elements[i].name + "=" + fieldValue;
        }
        //added for bug : 345219
        else if (document.forms[formCount].elements[i].type == "checkbox") {
            if (document.forms[formCount].elements[i].checked) {
                fieldValue = document.forms[formCount].elements[i].value;
                checkBoxName = document.forms[formCount].elements[i].name;
                checkBoxValues[checkBoxName] = {name: checkBoxName, value: fieldValue};
            } else {
                checkBoxName = document.forms[formCount].elements[i].name;
                checkBoxValues[checkBoxName] = {name: checkBoxName, value: "false"};
            }
        }
    }
    for (property in checkBoxValues) {
        formFieldValues += "&" + checkBoxValues[property].name + "=" + checkBoxValues[property].value;
    }
    formFieldValues += overflowMenuFormFieldValues;

    if (submitFunction != null && submitFunction != "" && submitProgram != null && submitProgram != "") {
        var objListWindow = findFrame(this, "listHidden");
        if (!objListWindow) {
            objListWindow = findFrame(this, "formViewHidden");
            if (!objListWindow) {
                objListWindow = findFrame(this, "hiddenFrame");
            }
        }
        var url = "";
        var timestamp = "";
        if (uiType != null && uiType != "" && uiType == "table") {
            var objListdis = findFrame(this, "listDisplay");
            if (objListdis.document.forms[0].timeStamp) {
                timestamp = objListdis.document.forms[0].timeStamp.value;
            }
        } else if (uiType != null && uiType == "structureBrowser") {
            timestamp = timeStamp;

        }
        if (strURL.indexOf("?") > 0) {
            strURL = strURL.substring(strURL.indexOf("?") + 1, strURL.length);
            url = "emxAEFSubmitJPO.jsp?submitFunction=" + submitFunction + "&submitProgram=" + submitProgram + "&" + strURL + "&timeStamp=" + timestamp + formFieldValues;
        } else {
            url = "emxAEFSubmitJPO.jsp?submitFunction=" + submitFunction + "&submitProgram=" + submitProgram + "&timeStamp=" + timestamp + formFieldValues;
        }


        var objForm = objListWindow.document.location;
        objForm.href = url;
    } else {
        //If the url is javascript simply evaluate it
        if (strURL.indexOf("javascript:") < 0) {
            //If submit function and submit program is given then invoke it in hidden frame
            if (formField == "textbox") {
                if (strTarget == "") {
                    strTarget = "popup";
                }
                submitToTargetLocation(strURL, strTarget, strFormFieldName, itemWidth, itemHeight, uiType, isModal, willSubmit, formFieldValues);
            } else if (formField == "combobox") {
                submitToTargetLocation(strURL, strTarget, strFormFieldName, itemWidth, itemHeight, uiType, isModal, willSubmit, formFieldValues);
            } else if (formField == "submit") {
                submitToTargetLocation(strURL, strTarget, strFormFieldName, itemWidth, itemHeight, uiType, isModal, willSubmit, formFieldValues);
            } else if (formField == "checkbox") {
                submitToTargetLocation(strURL, strTarget, strFormFieldName, itemWidth, itemHeight, uiType, isModal, willSubmit, formFieldValues);
            }
        } else {
            eval(strURL);
        }
    }
};

//This method will submit the href to the specified target location
function submitToTargetLocation(strURL, strTarget, strFormFieldName, itemWidth, itemHeight, uiType, isModal, willSubmit, formFieldValues) {
    var timestamp = "";
    var uiMode = "";
    var frameName = "";
    if (uiType != null && uiType != "" && uiType == "table") {
        uiMode = document.location + "";
        var objlistdis = findFrame(this, "listDisplay")
        if (uiMode.indexOf("emxTableEdit") > 0) {
            frameName = findFrame(parent, "formEditDisplay");
            timestamp = frameName.document.forms[0].timeStamp.value;
        } else if (objlistdis.document.forms[0].timeStamp) {
            //frameName = findFrame(parent,"listDisplay");
            timestamp = objlistdis.document.forms[0].timeStamp.value;
        }
    } else if (uiType != null && uiType == "structureBrowser") {
        timestamp = timeStamp;
    } else if (uiType != null && uiType != "" && uiType == "form") {
        timestamp = document.forms[0].timeStamp.value;
    }

    if (strTarget == "popup") {
        submitToPopup(strURL, strTarget, itemWidth, itemHeight, uiType, isModal, willSubmit, formFieldValues);
    } else if (strTarget == "searchContent") {
        getTopWindow().findSearchFrame(strURL, null, "test", strFormFieldName, null, "common", formFieldValues);
    } else {
        strURL += "&timeStamp=" + timestamp;
        var frameName = findFrame(parent, strTarget);
//This is added to do the findframe from the top level if it is not found from the parent level
        if (!frameName) {
            frameName = findFrame(getTopWindow(), strTarget);
        }
        if (!frameName) {
            frameName = findFrame(getTopWindow(), "content");
        }
        if (!frameName) {
            var strFeatures = "width=" + itemWidth + ", height=" + itemHeight + ",scrollbars=yes,toolbar=no,location=no,resizable=no,titlebar=no";
            var newWin1 = window.open("about:blank", "pDialog", strFeatures);
            document.forms[0].target = newWin1.name;
            document.forms[0].action = strURL;
            document.forms[0].method = "post";
            document.forms[0].submit();
        } else // Code adds the form Field values to the exisiting URL/Href and loads it in the strTarget frame
        {
            submitPost(strURL, frameName.name, formFieldValues, false);
        }
    }

}

//This method will submit the href to popup. In case of emxTable and structurebrowser the
//method to submit differs.
function submitToPopup(strURL, strTarget, itemWidth, itemHeight, uiType, isModal, willSubmit, formFieldValues) {
    var bPopup = ("popup" == strTarget) ? "true" : "false";
    var timestamp = "";
    var uiMode = "";
    if (uiType != null && uiType != "" && uiType == "table") {
        uiMode = document.location + "";
        var objlistdis = findFrame(this, "listDisplay");
        if (uiMode.indexOf("emxTableEdit") > 0) {
            frameName = findFrame(parent, "formEditDisplay");
            timestamp = frameName.document.forms[0].timeStamp.value;
        } else if (objlistdis.document.forms[0].timeStamp) {
            //frameName = findFrame(parent,"listDisplay");
            timestamp = objlistdis.document.forms[0].timeStamp.value;
        }
    } else if (uiType != null && uiType == "structureBrowser") {
        timestamp = timeStamp;

    } else if (uiType != null && uiType != "" && uiType == "form") {
        timestamp = document.forms[0].timeStamp.value;
    }

    if (uiType != null && uiType != "null" && uiType != "" && uiType == "table") {
        strURL += formFieldValues + "&timeStamp=" + timestamp;
        if (willSubmit == "true") {
            submitList(strURL, strTarget, "none", bPopup, itemWidth, itemHeight, null);
        } else {
            if (isModal == "true") {
                showModalDialog(strURL, itemWidth, itemHeight, true);
            } else {
                showNonModalDialog(strURL, itemWidth, itemHeight, true);
            }
        }
    } else if (uiType == "structureBrowser") {
        strURL += "&timeStamp=" + timestamp;
        if (willSubmit == "true") {
            submitFreezePaneData(strURL, strTarget, "none", bPopup, "true", itemWidth, itemHeight, null);
        } else {
            if (isModal == "true") {
                showModalDialog("../common/emxBlank.jsp", itemWidth, itemHeight, true);
                var objWindow = getTopWindow().modalDialog.contentWindow;
                //Added for Bug 345778
                submitPost(strURL, objWindow.name, formFieldValues, true);
            } else {
                //Last parameter to showNonModalDialog()is set to true to get new NonModalDialog window opened
                var objWindow = showNonModalDialog("../common/emxBlank.jsp", itemWidth, itemHeight, true, true);
                //Added for Bug 345778
                submitPost(strURL, objWindow.name, formFieldValues, true);
            }
        }
    } else {
        if (itemWidth == null || itemWidth == "") {
            itemWidth = "600";
        }
        if (itemHeight == null || itemHeight == "") {
            itemWidth = "600";
        }
        strURL += "&timeStamp=" + timestamp;
        if (isModal == "true") {
            showModalDialog("about:blank", itemWidth, itemHeight, true);
            var objWindow = getTopWindow().modalDialog.contentWindow;
            submitPost(strURL, objWindow.name, formFieldValues, true);
        } else {
            var objWindow = showNonModalDialog("about:blank", itemWidth, itemHeight, true, true);
            submitPost(strURL, objWindow.name, formFieldValues, true);
        }
    }
}

//This method handles the enter key in the textbox. It calls submitToolbarForm method which submits the form
//It also stops the content page form from submitting.
function isEnterKeyPressed(e, strURL, strTarget, strFormFieldName, itemWidth, itemHeight, uiType, submitFunction, submitProgram, isModal, willSubmit, formField, sCommandCode) {
    var pK = document.all ? window.event.keyCode : e.which;
    if (pK == 13) {
        document.forms[0].onsubmit = stopFormSubmit;
        if (eval(sCommandCode) && isValidSearchTextFTS(jQuery("#AEFGlobalFullTextSearch").val(), parseInt(emxUIConstants.FTS_MINIUM_SEARCHCHARS))) {
            eval(sCommandCode).main();
            return;
        } else if (strURL.length != 0 && !sCommandCode) {
            if (isValidSearchTextFTS(jQuery("#AEFGlobalFullTextSearch").val(), parseInt(emxUIConstants.FTS_MINIUM_SEARCHCHARS)))
                submitToolbarForm(strURL, strTarget, strFormFieldName, itemWidth, itemHeight, uiType, submitFunction, submitProgram, isModal, willSubmit, formField);
        } else {
            var submitObj = "";
            var elmLength = document.forms[0].elements.length;
            if (noOfButtons < 2) {
                for (var i = 0; i < elmLength; i++) {
                    if (document.forms[0].elements[i].type == "button") {
                        submitObj = document.forms[0].elements[i];
                        submitObj.click();
                        break;
                    }
                }
            }
        }
    }
}

//This method is used to stop the content form from being submitted in case of target location is popup.
function stopFormSubmit() {
    return false;
}


function textboxClicked(textBox) {
    jQuery('#AEFGlobalSearchHolder').css('display', 'none');
    if (textBox.id === "GlobalNewTEXT" && textBox.getAttribute("class") == null)
        textBox.value = "";
    textBox.setAttribute("class", "active");
    if (textBox.value != '') {
        jQuery("#searchClear").removeAttr("style");
    }
    textBox.focus();
    textBox.onfocus = function () {
        this.select();
    };
    /*var srchwidth = jQuery('#GTBsearchDiv').innerWidth();
    var srchcxtwidth = jQuery('.search-context').innerWidth();
    var srchactionswidth = jQuery('.search-actions').innerWidth();
    var tbwidth = srchwidth - srchcxtwidth - srchactionswidth;
    if(textBox.id === "GlobalNewTEXT")
    textBox.style.width = tbwidth + "px"*/
    ;
}

function textboxOnBlur(textBox) {
    jQuery('#AEFGlobalSearchHolder').css('display', 'none');
}

function clearSearch() {
    jQuery('#AEFGlobalSearchHolder').css('display', 'none');
    var tem = document.getElementById("GlobalNewTEXT");
    if (tem.getAttribute("class") == "active") {
        tem.removeAttribute("class");
        document.getElementById("searchClear").setAttribute("style", "display:none");
        tem.focus();
        tem.value = "Search";
    }
}

function searchTextBox(e) {
    var pK = "";
    if (typeof e != "undefined") {
        pK = document.all ? window.event.keyCode : e.which;
    }
    var tm = document.getElementById("GlobalNewTEXT");
    if (typeof e == "undefined" || pK == 13) {
        //do nothing
    } else {
        if (tm.getAttribute("class") == null && tm.value.indexOf("Search") == 0)
            tm.value = tm.value.replace("Search", "");
        if (tm.value != '') {
            document.getElementById("searchClear").removeAttribute("style");
        } else {
            document.getElementById("searchClear").setAttribute("style", "display:none");
        }
        tm.setAttribute("class", "active");
    }
}

//! Class emxUIToolbarFormField
function emxUIToolbarFormField(htmlControl, itemName, itemLabel, strURL, strTarget, actionLabel, uiType, itemWidth, itemHeight, htmlString, submitFunction, submitProgram, format, isModal, willSubmit, width, altText, strID, strJPOName, strMethodName, strCmdName, labelLength, strAlignment, sCommandCode, msValue, manualEdit, defaultActualValue, defaultOIDValue) {
    this.format = format;
    this.url = strURL;
    this.target = strTarget;
    delete this.superclass;
    this.formFieldName = itemName;
    this.actionLabel = actionLabel;
    this.htmlControl = htmlControl;
    this.strAlignment = strAlignment;
    var strDisplayText = itemLabel.htmlDecode();
    if (strDisplayText.length > strID) {
        strDisplayText = strDisplayText.substring(0, Math.min(strID, strDisplayText.length)) + "...";
    }
    strDisplayText = strDisplayText.htmlEncode();
    var strActionLabel = actionLabel.htmlDecode();
    if (strActionLabel.length > strID) {
        strActionLabel = actionLabel.substring(0, Math.min(strID, actionLabel.length)) + "...";
    }
    strActionLabel = strActionLabel.htmlEncode();

    this.itemLabel = itemLabel;
    var formName = "navigatorForm";
    if (document.forms[0]) {
        formName = document.forms[0].name;
    }
    if (uiType == "structureBrowser") {
        formName = "emxTableForm";
    }

    var actionWidth = "";
    if (width == null || width == "") {
        //width ="width:70px;";
    } else {
        width = "width:" + width + "px;";
        actionWidth = width;
    }
    switch (htmlControl) {

        case "textbox":
            if (format != null && format == "fullsearch") {
                this.html = "<span class=\"left-cap\"></span>";
                this.html += "<span class=\"search-input\"><input class=\"search-widget\" type=\"text\" size=\"30\" id=\"" + itemName + "\" name=\"" + itemName + "\"value=\"Search\"";
                this.html += " onBlur=\"textboxOnBlur(this)\" onClick=\"textboxClicked(this)\"  onKeyPress=\"isEnterKeyPressed(event, '" + this.url + "' , '" + this.target + "' , '" + this.formFieldName + "' , '" + itemWidth + "' , '" + itemHeight + "' , '" + uiType + "' , '" + submitFunction + "' , '" + submitProgram + "' , '" + isModal + "' , '" + willSubmit + "' , 'textbox','" + sCommandCode + "' )\"/>";
                this.html += "<a class=\"btn clear\" href=\"\"></a>";
                this.html += "</span>";
                this.html += "<span class=\"right-cap\"></span>";
                this.html += "</li>";
            }
            // If the format = chooser  in Textbox enter key press should not do anything
            else if (format != null && format != "chooser" && format != "date") {
                this.html = "&nbsp;<label title=\"" + altText + "\" class=\"toolbar-panel-label\">" + strDisplayText + "</label>" + "&nbsp;<input type=\"" +
                    "text" + "\" class=text  title=\"" + altText + "\" id=\"" + itemName + "\" name=\"" +
                    itemName + "\" value=\"" + htmlString + "\" onClick=\"textboxClicked(this)\"  onKeyPress=\"isEnterKeyPressed(event, '" + this.url + "' , '" + this.target + "' , '" + this.formFieldName + "' , '" + itemWidth + "' , '" + itemHeight + "' , '" + uiType + "' , '" + submitFunction + "' , '" + submitProgram + "' , '" + isModal + "' , '" + willSubmit + "' , 'textbox','" + sCommandCode + "' )\" value= ''";
                if (width != null && width != "") {
                    this.html += " style=\"" + width + "\"";
                }
            } else {
                this.html = "&nbsp;<label title=\"" + altText + "\" class=\"toolbar-panel-label\">" + strDisplayText + "</label>" + "&nbsp;<input type=\"" +
                    "text" + "\" class=text  title=\"" + altText + "\" id=\"" + itemName + "\" name=\"" +
                    itemName + "\" value=\"" + htmlString + "\"  value= ''";
                if (width != null && width != "") {
                    this.html += " style=\"" + width + "\"";
                }
            }
            // To make the Textbox readonly for type/vault chooser and date
            if (format != null && format == "date") {
                this.html += "readonly";
            } else if (format != null && format == "chooser" && manualEdit != 'true') {
                this.html += "readonly";
            }
            if (sCommandCode && format != null && format != "fullsearch") {
                this.html += " onchange=\"submitToolbarForm('','','','','','','','','','','','" + sCommandCode + "')\"";
            }

            if (format != null && format == "fullsearch") {
            } else {
                this.html += "/>";
            }
            if (format != null && format == "chooser") {
                var hiddenField = itemName + "_actualValue";
                var hiddenFieldOID = itemName + "_OID";
                this.html += "<input id=\"" + hiddenField + "\" name=\"" + hiddenField + "\" type=hidden value=\"" + defaultActualValue + "\" />";
                this.html += "<input id=\"" + hiddenFieldOID + "\" name=\"" + hiddenFieldOID + "\" type=hidden value=\"" + defaultOIDValue + "\" />";
            }
            if (format != null && format == "date") {
                /*
                showCalendar will take inputTime parameter to set the selected item in the calendar.
                The actual date value is read from the xml and passed to showCalendar method
                Start
                */
                var inputTime = msValue;
                if (inputTime && inputTime != null && inputTime != "") {
                    inputTime = new Number(inputTime);
                    var dateObj = new Date(inputTime);

                    var month = dateObj.getMonth() + 1;
                    var day = dateObj.getDate();
                    var year = dateObj.getFullYear();
                    var hours = dateObj.getHours();
                    var minutes = dateObj.getMinutes();
                    var seconds = dateObj.getSeconds();

                    var ampm = "AM";
                    if (new Number(hours) > 11 && new Number(hours) != 12) {
                        hours = hours - 12;
                        ampm = "PM";
                    }
                    inputTime = month + "/" + day + "/" + year + " " + hours + ":" + minutes + ":" + seconds + " " + ampm;
                } else {
                    inputTime = "";
                }
                //End

                this.html += "&nbsp;";
                this.html += "<a href=\"javascript:showCalendar('" + formName + "' , '" + itemName + "' , '" + inputTime + "' ,'' ,'' ,'')\">";
                this.html += "<img src=\"../common/images/iconSmallCalendar.gif\"  style=\"display:inline\" border=\"0\" valign=\"absmiddle\" name = img5></a>";
                this.html += "<input type=\"hidden\" name=\"" + itemName + "_msvalue\" value=\"" + msValue + "\"/>";
            }
            // For textbox with type/vault chooser
            if (format != null && format == "chooser") {
                this.html += "&nbsp;";
                if (manualEdit == 'true') {
                    this.html += "<input type='button' name='chooser' class='button' value='...' style=\"\" title=\"" + altText + "\" onClick=\"submitToolbarForm('" + this.url + "&formName=" + formName + "&fieldNameActual=" + itemName + "&fieldNameDisplay=" + itemName + "', '" + this.target + "' , '" + this.formFieldName + "' , '" + itemWidth + "' , '" + itemHeight + "' , '" + uiType + "' , '" + submitFunction + "' , '" + submitProgram + "' , '" + isModal + "' , '" + willSubmit + "' , 'submit'" + ")\">";
                } else {
                    this.html += "<input type='button' name='chooser' class='button' value='...' style=\"\" title=\"" + altText + "\" onClick=\"submitToolbarForm('" + this.url + "&formName=" + formName + "&fieldNameActual=" + itemName + "&fieldNameDisplay=" + itemName + "', '" + this.target + "' , '" + this.formFieldName + "' , '" + itemWidth + "' , '" + itemHeight + "' , '" + uiType + "' , '" + submitFunction + "' , '" + submitProgram + "' , '" + isModal + "' , '" + willSubmit + "' , 'submit', '" + sCommandCode + "')\">";
                }
                noOfButtons++;
            }
            // Button will  be displayed if url specified and setting Action Label is present
            if (actionLabel.length != 0 && strURL != null && strURL != "") {
                this.html += "&nbsp;<input type=\"button\" name=\"" + itemName + "\" class=\"button\" style=\"\" title=\"" + altText + "\" onClick=\"submitToolbarForm('" + this.url + "' , '" + this.target + "' , '" + this.formFieldName + "' , '" + itemWidth + "' , '" + itemHeight + "' , '" + uiType + "' , '" + submitFunction + "' , '" + submitProgram + "' , '" + isModal + "' , '" + willSubmit + "' , 'submit', '" + sCommandCode + "')\" onKeyPress=\"isEnterKeyPressed(event, '" + this.url + "' , '" + this.target + "' , '" + this.formFieldName + "' , '" + itemWidth + "' , '" + itemHeight + "' , '" + uiType + "' , '" + submitFunction + "' , '" + submitProgram + "' , 'submit', '" + sCommandCode + "')\" value=\"" + strActionLabel + "\" />";
                noOfButtons++;
            }
            /*if(format!=null && format=="fullsearch"){
                this.html += "&nbsp;<img src=\"../common/images/searchPLMTB.png\" border=\"0\" align=\"top\" style=\"cursor: pointer;\" name=\""+itemName+ "\" class=\"button\" title=\""+altText+"\" onClick=\"submitToolbarForm('"+this.url+"' , '"+this.target+"' , '"+this.formFieldName+"' , '"+itemWidth+"' , '"+itemHeight+"' , '"+uiType+"' , '"+submitFunction+"' , '"+submitProgram+"' , '"+isModal+"' , '"+willSubmit+"' , 'submit', '"+ sCommandCode +"')\" onKeyPress=\"isEnterKeyPressed(event, '"+this.url + "' , '" + this.target +"' , '"+this.formFieldName+"' , '"+itemWidth+"' , '"+itemHeight+"' , '"+uiType+"' , '"+submitFunction+"' , '"+submitProgram+"' , 'submit', '"+ sCommandCode +"')\" value=\""+strActionLabel+"\" />";
            }*/
            if (format != null && format == "fullsearch") {
                this.html += "&nbsp;<img src=\"../common/images/searchPLMTB.png\" border=\"0\" align=\"top\" style=\"cursor: pointer;\" name=\"" + itemName + "\" class=\"button\" title=\"" + altText + "\" onClick=\"validateSearchText('" + this.url + "' , '" + this.target + "' , '" + this.formFieldName + "' , '" + itemWidth + "' , '" + itemHeight + "' , '" + uiType + "' , '" + submitFunction + "' , '" + submitProgram + "' , '" + isModal + "' , '" + willSubmit + "' , 'submit', '" + sCommandCode + "')\" onKeyPress=\"isEnterKeyPressed(event, '" + this.url + "' , '" + this.target + "' , '" + this.formFieldName + "' , '" + itemWidth + "' , '" + itemHeight + "' , '" + uiType + "' , '" + submitFunction + "' , '" + submitProgram + "' , 'submit', '" + sCommandCode + "')\" value=\"" + strActionLabel + "\" />";
            }
            break;
        case "combobox":
            this.html = htmlString;
            if (actionLabel.length != 0) {
                this.html += "&nbsp;<input type=\"button\" name=\"" + itemName + "\" class=\"button\" style=\"\" title=\"" + altText + "\" onClick=\"submitToolbarForm('" + this.url + "' , '" + this.target + "' , '" + this.formFieldName + "' , '" + itemWidth + "' , '" + itemHeight + "' , '" + uiType + "' , '" + submitFunction + "' , '" + submitProgram + "' , '" + isModal + "' , '" + willSubmit + "' , 'submit', '" + sCommandCode + "')\" onKeyPress=\"isEnterKeyPressed(event, '" + this.url + "' , '" + this.target + "' , '" + this.formFieldName + "' , '" + itemWidth + "' , '" + itemHeight + "' , '" + uiType + "' , '" + submitFunction + "' , '" + submitProgram + "' , 'submit', '" + sCommandCode + "')\" value=\"" + strActionLabel + "\" />";
                noOfButtons++;
            }
            break;
        case "submit":
            this.html = "<input type=\"button\" name=\"" + itemName + "\" class=\"button\" style=\"" + actionWidth + "\" title=\"" + altText + "\" onClick=\"submitToolbarForm('" + this.url + "' , '" + this.target + "' , '" + this.formFieldName + "' , '" + itemWidth + "' , '" + itemHeight + "' , '" + uiType + "' , '" + submitFunction + "' , '" + submitProgram + "' , '" + isModal + "' , '" + willSubmit + "' , 'submit', '" + sCommandCode + "')\" onKeyPress=\"isEnterKeyPressed(event, '" + this.url + "' , '" + this.target + "' , '" + this.formFieldName + "' , '" + itemWidth + "' , '" + itemHeight + "' , '" + uiType + "' , '" + submitFunction + "' , '" + submitProgram + "' , 'submit', '" + sCommandCode + "')\" value=\"" + strActionLabel + "\" />";
            noOfButtons++;
            break;
        case "checkbox":
            // Modified for bug 347655
            if (htmlString == "true" || htmlString == "TRUE")
                this.html = "&nbsp;<input type=\"" + htmlControl + "\" id=\"" + itemName + "\" name=\"" + itemName + "\" value=\"true\" checked onClick=\"submitToolbarForm('" + this.url + "' , '" + this.target + "' , '" + this.formFieldName + "' , '" + itemWidth + "' , '" + itemHeight + "' , '" + uiType + "' , '" + submitFunction + "' , '" + submitProgram + "' , '" + isModal + "' , '" + willSubmit + "' , 'checkbox','" + sCommandCode + "' )\"/> <label title=\" " + altText + " \" class=\"toolbar-panel-label\">" + strDisplayText + "&nbsp;</label>";
            else
                this.html = "&nbsp;<input type=\"" + htmlControl + "\" id=\"" + itemName + "\" name=\"" + itemName + "\" value=\"true\" onClick=\"submitToolbarForm('" + this.url + "' , '" + this.target + "' , '" + this.formFieldName + "' , '" + itemWidth + "' , '" + itemHeight + "' , '" + uiType + "' , '" + submitFunction + "' , '" + submitProgram + "' , '" + isModal + "' , '" + willSubmit + "' , 'checkbox','" + sCommandCode + "' )\"/> <label title=\" " + altText + " \" class=\"toolbar-panel-label\">" + strDisplayText + "&nbsp;</label>";
            if (actionLabel.length != 0) {
                this.html += "&nbsp;<input type=\"button\" name=\"" + itemName + "\" class=\"button\" style=\"\" title=\"" + altText + "\" onClick=\"submitToolbarForm('" + this.url + "' , '" + this.target + "' , '" + this.formFieldName + "' , '" + itemWidth + "' , '" + itemHeight + "' , '" + uiType + "' , '" + submitFunction + "' , '" + submitProgram + "' , '" + isModal + "' , '" + willSubmit + "' , 'submit', '" + sCommandCode + "')\" onKeyPress=\"isEnterKeyPressed(event, '" + this.url + "' , '" + this.target + "' , '" + this.formFieldName + "' , '" + itemWidth + "' , '" + itemHeight + "' , '" + uiType + "' , '" + submitFunction + "' , '" + submitProgram + "' , 'submit', '" + sCommandCode + "')\" value=\"" + strActionLabel + "\" />";
                noOfButtons++;
            }
            break;
    }
    this.element = null;
    this.text = this.html;
}

emxUIToolbarFormField.prototype.url = "";
emxUIToolbarFormField.prototype.target = "";
emxUIToolbarFormField.prototype.htmlControl = "";
emxUIToolbarFormField.prototype.formFieldName = "";
emxUIToolbarFormField.prototype.itemLabel = "";
emxUIToolbarFormField.prototype.actionLabel = "";

emxUIToolbarFormField.prototype = new emxUIToolbarButton;
emxUIToolbarFormField.prototype.init = function _emxUIToolbarFormField_init(objParent) {
    if (objParent.nodeName == "UL") {
        this.element = document.createElement("li");
        if (this.format != null && this.format == "fullsearch") {
            this.element.className = "search-widget";
        }
    } else {
        this.element = document.createElement("td");
    }
    //this.element.className = "toolbar-panel-label";
    this.element.innerHTML = this.html;
    if (emxUIConstants.UI_AUTOMATION == 'true') {
        this.element.setAttribute('data-aid', this.formFieldName);
    }
    objParent.appendChild(this.element);
    this.createDOM();
}

emxUIToolbarFormField.prototype.emxUIBaseCreateDOM = emxUIToolbarFormField.prototype.createDOM;
emxUIToolbarFormField.prototype.createDOM = function _emxUIToolbarFormField_createDOM(objParent) {
    if (this.grayout == "true") {
        this.disable();
    }
    if (this.parent.emxClassName == "emxUIToolbarMenu") {
        this.element = document.createElement("li");
        this.element.setAttribute("class", "input");
        this.element.innerHTML = this.html;
        var objThis = this;
        this.element.onmousedown = function () {
            var objEvent = emxUICore.getEvent(objThis.parent && objThis.parent.displayWindow ? objThis.parent.displayWindow : null);
            objEvent.cancelBubble = true;
        };
        return this.element;
    }
};

emxUIToolbarFormField.prototype.anchorStateChange = function _emxUIToolbarFormField_anchorStateChange(anchorElement, disable) {

    var innerHTMLText = anchorElement.innerHTML;
    if (disable) {
        var href = anchorElement.getAttribute("href");
        if (href.indexOf("javascript:showCalendar") > -1) {
            innerHTMLText = innerHTMLText.replace('/iconSmallCalendar.gif', '/iconSmallCalendarDisabled.gif');
            anchorElement.innerHTML = innerHTMLText;
        }
        if (href && href != "" && href != null) {
            anchorElement.setAttribute('href_bak', href);
        }
        anchorElement.removeAttribute('href');
    } else {
        var href_bak = anchorElement.getAttribute("href_bak");
        if (href_bak != null && href_bak.indexOf("javascript:showCalendar") > -1) {
            innerHTMLText = innerHTMLText.replace('/iconSmallCalendarDisabled.gif', '/iconSmallCalendar.gif');
            anchorElement.innerHTML = innerHTMLText;
        }
        if (anchorElement.getAttribute("href_bak") != null) {
            anchorElement.setAttribute('href', anchorElement.attributes['href_bak'].nodeValue);
        }
    }
};

emxUIToolbarFormField.prototype.stateChange = function _emxUIToolbarFormField_stateChange(disable) {
    if (this.element) {
        var children = this.element.children ? this.element.children : this.element.childNodes;

        for (var ci = 0; ci < children.length; ci++) {
            var htmlChild = children[ci];
            if (htmlChild.tagName == "A") {
                this.anchorStateChange(htmlChild, disable);
            } else {
                htmlChild.disabled = disable;
            }
        }
    }
};
emxUIToolbarFormField.prototype.emxUIToolbarItemDisable = emxUIToolbarFormField.prototype.disable;
emxUIToolbarFormField.prototype.disable = function _emxUIToolbarFormField_disable() {
    //this.emxUIToolbarItemDisable();
    this.enabled = false;
    this.stateChange(true);
};
emxUIToolbarFormField.prototype.emxUIToolbarItemEnable = emxUIToolbarFormField.prototype.enable;
emxUIToolbarFormField.prototype.enable = function _emxUIToolbarFormField_enable() {
    //this.emxUIToolbarItemEnable();
    this.enabled = true;
    this.stateChange(false);
};

/******ixk: dynamic label********/
function emxUIToolbarLabelField(format, itemName, itemLabel, strAlignment) {
    this.itemName = itemName;
    this.itemLabel = itemLabel;
    this.text = itemLabel;
    this.strAlignment = strAlignment;
}

emxUIToolbarLabelField.prototype = new emxUIToolbarButton;
emxUIToolbarLabelField.prototype.init = function _emxUIToolbarLabelField_init(objParent) {
    //this.html = "<div id='"+this.itemName+"'>"+this.itemLabel+"</div>";
    this.html = this.itemLabel;
    this.createDOM(objParent);
};

emxUIToolbarLabelField.prototype.createDOM = function _emxUIToolbarLabelField_createDOM(objParent) {
    if (objParent.nodeName == "UL") {
        this.element = document.createElement("li");
    } else {
        this.element = document.createElement("td");
    }
    this.element.id = this.itemName;
    if (emxUIConstants.UI_AUTOMATION == 'true') {
        this.element.setAttribute('data-aid', this.itemName);
    }
    //this.element.className = "toolbar-panel-label";
    if (this.itemName == 'AEFWelcomeToolbar') {
        this.element.innerHTML = "<span>" + this.html + "</span>";
    } else {
        this.element.innerHTML = this.html;
    }


    objParent.appendChild(this.element);
};

/******ixk: dynamic label********/

// Added for Toolbar enhancement code - End

//Added for Bug 345778
function getKeyValuePairs(formFieldValues) {
    var retformFieldValues = [];
    var array = formFieldValues.split('&');
    for (var i = 0; i < array.length; i++) {
        var item = array[i];
        var fieldName = item.substring(0, item.indexOf("="));
        var fieldValue = item.substring(item.indexOf("=") + 1);
        retformFieldValues[i] = {name: fieldName, value: fieldValue};
    }
    return retformFieldValues;
}


function getFinalURLforTreeFrames(hostNameDetails, currObjId) {
    var objParent = null;
    if (getTopWindow().objStructureTree) {
        objParent = getTopWindow().objStructureTree.getSelectedNode();
    }
    var finalCallURL = "";
    var strTarget = "content";
    if (objParent != null && objParent != 'undefined' && objParent != "undefined" && objParent != "") {
        var refObjectID = objParent.objectID;
        if (refObjectID == currObjId) {
            finalCallURL = "/common/emxNavigator.jsp?targetLocation=" + escape(strTarget) + "&objectId=" + refObjectID;
            finalCallURL = finalCallURL.replace("..", "");
            finalCallURL = hostNameDetails + finalCallURL;
        } else {
            finalCallURL = "/common/emxNavigator.jsp?targetLocation=" + escape(strTarget) + "&objectId=" + currObjId;
            finalCallURL = finalCallURL.replace("..", "");
            finalCallURL = hostNameDetails + finalCallURL;
        }
    }
    return finalCallURL;
}

//displays Page URL when clicked on Page URL Icon
function showPageURL(objThis) {
    if (!checkForPageURLDiv()) {
        var frame = "";
        var documentFrame = null;
        var treeFrameURL = "";
        var contextWindow = emxUICore.findFrame(getTopWindow(), "content");//window.parent.parent;
        if (contextWindow) {
            contextWindow = contextWindow.document;
        } else {
            contextWindow = document;
        }
        var isTreeFrame = (contextWindow.location.href.indexOf('emxTree.jsp') > -1) ? true : false;
        var isNavigatorFrame = (contextWindow.location.href.indexOf('emxNavigator.jsp') > -1) ? true : false;
        var lastIndexOfCommon = contextWindow.location.href.indexOf("common");
        var hostNameDetails = contextWindow.location.href.substring(0, lastIndexOfCommon - 1);

        var startIndexOfObjectId = contextWindow.location.href.indexOf("objectId", 0);
        var lastIndexOfObjectId = (contextWindow.location.href.indexOf("&", startIndexOfObjectId) > -1) ? contextWindow.location.href.indexOf("&", startIndexOfObjectId) : contextWindow.location.href.length;
        var currObjId = contextWindow.location.href.substring(startIndexOfObjectId + 9, lastIndexOfObjectId);

        if (isTreeFrame) {
            treeFrameURL = getFinalURLforTreeFrames(hostNameDetails, currObjId);
            frame = getRequiredFrame();
            documentFrame = frame.document;
        } else {
            frame = getRequiredFrame();
        }
        if (!isTreeFrame || isNavigatorFrame) {
            if (frame) {
                documentFrame = frame.document;
            } else {
                documentFrame = document;
            }
        }
        var pageURLDiv = documentFrame.createElement("div");
        pageURLDiv.className = "pageURLDiv";
        pageURLDiv.id = "pageURLDiv";
        pageURLDiv.name = "pageURLDiv";
        var intX = objThis.element.offsetLeft + 15;
        var intY = objThis.element.offsetTop;
        if (objMainToolbar.container && objMainToolbar.container.className != "toolbar-frame folded") {
            intX += objMainToolbar.element.clientWidth;
        }
        if (strUIType == "structureBrowser") {
            var phd = document.getElementById("pageHeadDiv");
            var ht = phd.clientHeight;
            if (ht <= 0) {
                ht = phd.offsetHeight;
            }
            intY = ht;
        }
        pageURLDiv.style.top = intY + "px";
        pageURLDiv.style.left = intX + "px";
        var textfield = documentFrame.createElement("input");
        textfield.className = "pageURL";
        textfield.setAttribute("type", "text");
        textfield.setAttribute("id", "pageURLTextBox");
        pageURLDiv.appendChild(textfield);
        setTimeout(function () {
            textfield.focus();
            textfield.select();
        }, 50);
        var strTextURL = "";
        /*if(strUIType=="structureBrowser"){
          document.body.appendChild(pageURLDiv);
          if(isFullSearch == "true"){
              strTextURL = getTopWindow().location.href;
          }else{
              strTextURL = document.location.href;
            //textfield.setAttribute("value",treeFrameURL);
          }
        }else*/
        if (strUIType == "form") {
            if (isIE && document.getElementById("divPageBody")) {
                document.getElementById("divPageBody").appendChild(pageURLDiv);
            } else {
                document.body.appendChild(pageURLDiv);
            }
            strTextURL = document.location.href;
        } else if (frame) {
            var reqdDiv = document.getElementById("divPageBody");
            if (isIE && reqdDiv) {
                reqdDiv.appendChild(pageURLDiv);
            } else {
                documentFrame.body.appendChild(pageURLDiv);
            }
            if (isTreeFrame && treeFrameURL != "") {
                strTextURL = treeFrameURL;
            } else if (document.location.href.indexOf("common/emxSearchHeader.jsp?") > 0) {
                //In General Search Page, traverse two levels
                strTextURL = frame.parent.parent.document.location.href;
            } else {
                if (strUIType == "structureBrowser") {
                    if (isFullSearch == "true") {
                        strTextURL = getTopWindow().location.href;
                    } else {
                        strTextURL = document.location.href;
                    }
                } else {
                    strTextURL = frame.parent.document.location.href;
                }
            }
        } else if (document.body) {
            document.body.appendChild(pageURLDiv);
            strTextURL = document.location.href;
        }
        if (isIE) {
            if (strTextURL.indexOf("treeLabel") > 0) {
                var index1 = strTextURL.indexOf("treeLabel=") + 10;
                var index2 = strTextURL.indexOf("&", index1) > -1 ? strTextURL.indexOf("&", index1) : strTextURL.length();
                var subString = encodeURIComponent(strTextURL.substring(index1, index2));
                strTextURL = updateURLParameter(strTextURL, "treeLabel", subString);
            }
        }
        // oeo Fix for IR-095885V6R2012
        // URL is already encoded except for spaces, so global replace only the spaces.
        textfield.setAttribute("value", strTextURL.replace(new RegExp(" ", "gi"), "%20"));

        addEvent(pageURLDiv, "mouseout", function () {
            setTimeout("removePageURLDiv()", 1000);
        });
        //Fix for IR-021921V6R2011
        var deviation = pageURLDiv.offsetWidth + intX;
        if (deviation > documentFrame.body.offsetWidth) {
            deviation = deviation - documentFrame.body.offsetWidth;
            pageURLDiv.style.left = (intX - deviation) + "px";
        }
        //Fix for IR-021921V6R2011
    }
}

function checkForPageURLDiv() {
    var divExists = false;
    if (document.getElementById("pageURLDiv")) {
        divExists = true;
    } else {
        var otherFrames = getRequiredFrame();
        if (otherFrames && otherFrames.document.getElementById("pageURLDiv")) {
            divExists = true;
        }
    }
    return divExists;
}

function removePageURLDiv() {
    if (document.location.href.indexOf("common/emxCreate.jsp?") > 0) {
        if (isIE && document.getElementById("divPageBody")
            && document.getElementById("pageURLDiv")) {
            document.getElementById("divPageBody").removeChild(document.getElementById("pageURLDiv"));
            return;
        }
    }
    if (document.getElementById("pageURLDiv")) {
        var frame = getRequiredFrame();
        var reqdDiv = document.getElementById("divPageBody");
        if (isIE && frame) {
            if (strUIType == "structureBrowser") {
                document.body.removeChild(document.getElementById("pageURLDiv"));
            } else if (reqdDiv) {
                reqdDiv.removeChild(document.getElementById("pageURLDiv"));
            }
            return;
        } else {
            document.body.removeChild(document.getElementById("pageURLDiv"));
            return;
        }
    }
    var otherFrame = getRequiredFrame();
    if (otherFrame && otherFrame.document.getElementById("pageURLDiv")) {
        otherFrame.document.body.removeChild(otherFrame.document.getElementById("pageURLDiv"));
    }
}

//Get Required Frame to insert or delete pageURLDiv
function getRequiredFrame() {
    var frameToBeReturned = null;
    if (strUIType == "table") {
        var uiMode = "";
        uiMode = document.location + "";
        if (uiMode.indexOf("emxTableEdit") > 0) {
            frameToBeReturned = emxUICore.findFrame(this, "formEditDisplay");
        } else {
            frameToBeReturned = emxUICore.findFrame(this, "listDisplay");
        }
    } else if (strUIType == "form") {

    } else if (strUIType == "structureBrowser") {
        frameToBeReturned = emxUICore.findFrame(parent, "content");
    } else {
        if (document.location.href.indexOf("emxPortalToolbar.jsp?") > 0) {
            //Portal page
            return emxUICore.findFrame(parent, "portalDisplay");
        } else if (document.location.href.indexOf("emxLifecycleHeader.jsp?") > 0) {
            //Life Cycle Page
            return emxUICore.findFrame(parent, "pagecontent");
        } else if (document.location.href.indexOf("common/emxSearchHeader.jsp?") > 0) {
            //General Search Page
            return emxUICore.findFrame(parent, "searchContent");
        }
    }
    if (frameToBeReturned == null || frameToBeReturned == "")
        frameToBeReturned = this.window;
    return frameToBeReturned;
}

//Added for Bug 345778
var URL, TARGET, FORMFIELDVALUES, ISPOPUP;

function submitPost(url, target, formFieldValues, isPopup) {
    var objViewHiddenWindow = findFrame(window, "submitHiddenFrame");
    URL = url;
    TARGET = target;
    FORMFIELDVALUES = formFieldValues;
    ISPOPUP = isPopup;
    if (!objViewHiddenWindow) {
        objViewHiddenWindow = document.createElement("IFRAME");
        objViewHiddenWindow.width = "0%";
        objViewHiddenWindow.height = "0%";
        objViewHiddenWindow.name = "submitHiddenFrame";
        objViewHiddenWindow.src = "emxBlank.jsp";
        document.body.appendChild(objViewHiddenWindow);
        if (!isIE || (isIE && isMinIE10)) {
            setTimeout("submitPost(URL,TARGET,FORMFIELDVALUES,ISPOPUP)", 50);
            return;
        }
    }

    formFieldValues = getKeyValuePairs(formFieldValues);
    jQuery(objViewHiddenWindow).ready(function () {
        var objViewHiddenWindow = findFrame(window, "submitHiddenFrame");
        var form = jQuery("<form name='emxHiddenForm' id='emxHiddenForm'/>");
        for (var index = 0; index < formFieldValues.length; index++) {
            var input = jQuery("<input type='hidden' name='" + formFieldValues[index].name + "' id='" + formFieldValues[index].name + "' value='" + formFieldValues[index].value + "'/>");
            form.append(input);
        }

        var oldform = jQuery("form#emxHiddenForm", jQuery(objViewHiddenWindow.document));
        if (oldform) {
            oldform.remove();
        }
        //jQuery(objViewHiddenWindow.document).append(form);
        if (!jQuery(objViewHiddenWindow.document.body)[0]) {
            var html = document.createElement("html");
            objViewHiddenWindow.document.appendChild(html);
            var body = document.createElement("body");
            jQuery(jQuery('html', objViewHiddenWindow.document)).append(body);
        }
        jQuery(objViewHiddenWindow.document.body).append(form);


        if (!isPopup) {
            var parentFrame = findFrame(parent, "detailsDisplay");

            if (!parentFrame) {
                parentFrame = getTopWindow();
            }
            target = findFrame(parentFrame, target);
            target = target.name;
        }
        var formSubmit = form[0];
        if (jQuery.inArray(target, HIDDEN_FRAME_LIST) != -1) {
            addSecureToken(formSubmit);
        }
        formSubmit.setAttribute("accept-charset", "UTF-8");
        formSubmit.action = url;
        formSubmit.method = "post";
        formSubmit.target = target;
        formSubmit.submit();
        overflowMenuFormFieldValues = '';
        if (jQuery.inArray(target, HIDDEN_FRAME_LIST) != -1) {
            removeSecureToken(formSubmit);
        }
    });
}


function closeStructureNavigatorFrame() {


    var obj;
    var contentFrame;
    var objFrameset;
    for (var i = 0; i < objMainToolbar.items.length; i++) {
        var iurl = objMainToolbar.items[i].url;
        if (iurl && iurl.indexOf("closeStructureNavigatorFrame()") >= 0) {
            obj = objMainToolbar.items[i];
            break;
        }
    }

    if (obj) {
        if (obj.element) {
            toggleControlButton(obj, "iconActionClosePanel.gif", "iconActionOpenPanel.gif", emxUIConstants.STR_NAV_CLOSE, emxUIConstants.STR_NAV_OPEN, emxUIConstants.STR_CLOSE, emxUIConstants.STR_OPEN);
            obj.url = "javascript:void(openStructureNavigatorFrame())";
        }
    }
    contentFrame = getTopWindow().findFrame(getTopWindow(), "content");
    objFrameset = contentFrame.document.getElementById("emxStructureNavigatorFrame");
    if (objFrameset != null) {
        objFrameset = objFrameset;
    } else {
        contentFrame = getTopWindow().findFrame(getTopWindow(), "treeContent");
        objFrameset = contentFrame.document.getElementById("emxStructureNavigatorFrame");
    }
    var cols = objFrameset.getAttribute("cols");
    //To retain the frameset width (column values) in IE during open/close of framset--http://support.microsoft.com/kb/981303
    if (isIE) {
        cols = cols.split(",")[0];
        if (cols.length >= 4) {
            var indx = cols.length - 2;
            cols = cols.substring(0, indx);
        }
    } else {
        cols = cols.split(",")[0];
    }
    if (getTopWindow().StructureNavigator) {
        getTopWindow().StructureNavigator.setCols(cols);
    }

    var treeCols = parseInt(cols);
    while (treeCols > 0) {
        treeCols = treeCols - 10;
        objFrameset.setAttribute("cols", treeCols + "0,*,0");
    }
    //Added For IE10, in IE10 change in cols attribute is not reflected, need to add padding
    objFrameset.style.padding = '0px';
}

function toggleControlButton(obj, imageoldName, imageNewName, defaultOldText, defaultNewText, customOldText, customNewText) {

    strText = obj.element.innerHTML;
    if (strText.indexOf(">") > -1) {
        var strText1 = [];
        strText1 = strText.split(">");
        var imageName = strText1[0];
        var textLabel;
        textLabel = obj.element.title;

        imageName = imageName.replace(imageoldName, imageNewName);
        obj.icon = "../common/images/" + imageNewName;
        if (textLabel.indexOf(defaultOldText) > -1) {
            textLabel = textLabel.replace(defaultOldText, defaultNewText);
            obj.element.title = textLabel;
        } else {
            textLabel = textLabel.replace(customOldText, customNewText);
            obj.element.title = textLabel;
        }
        obj.element.title = textLabel;
        obj.text = textLabel;

        if (obj.parent.maxLabelChars > -1 && textLabel.length > obj.parent.maxLabelChars) {
            textLabel = textLabel.substring(0, Math.min(obj.parent.maxLabelChars, textLabel.length)) + "...";
        }
        strText = imageName + ">";
        obj.element.innerHTML = strText;
    }
    var currMode = obj.element.title;
    if (getTopWindow().StructureNavigator) {
        getTopWindow().StructureNavigator.setCurrButton(currMode);
    }

}

function openStructureNavigatorFrame() {

    var obj;
    var contentFrame;
    var objFrameset;
    var colCount;
    var treeColsLength = 200;
    if (getTopWindow().StructureNavigator) {
        colCount = getTopWindow().StructureNavigator.getCols();
        if (colCount != null) {
            treeColsLength = parseInt(colCount.split(",")[0]);
        }
    }
    contentFrame = getTopWindow().findFrame(getTopWindow(), "content");
    objFrameset = contentFrame.document.getElementById("emxStructureNavigatorFrame");
    if (objFrameset != null) {
        objFrameset = objFrameset;
    } else {
        contentFrame = getTopWindow().findFrame(getTopWindow(), "treeContent");
        objFrameset = contentFrame.document.getElementById("emxStructureNavigatorFrame");
    }
    var cols = objFrameset.getAttribute("cols");
    var treeCols = parseInt(cols.split(",")[0]);
    while (treeCols < treeColsLength) {
        treeCols = treeCols + 10;
        objFrameset.setAttribute("cols", treeCols + ",*,0");
    }
    //Added For IE10, in IE10 change in cols attribute is not reflected, need to add padding
    objFrameset.style.padding = '1px';

    for (var i = 0; i < objMainToolbar.items.length; i++) {
        var iurl = objMainToolbar.items[i].url;

        if (iurl && iurl.indexOf("openStructureNavigatorFrame()") >= 0) {
            obj = objMainToolbar.items[i];
            break;
        }
    }

    if (obj) {
        if (obj.element) {
            toggleControlButton(obj, "iconActionOpenPanel.gif", "iconActionClosePanel.gif", emxUIConstants.STR_NAV_OPEN, emxUIConstants.STR_NAV_CLOSE, emxUIConstants.STR_OPEN, emxUIConstants.STR_CLOSE);
            obj.url = "javascript:void(closeStructureNavigatorFrame())";
        }
    }
}


function closeFrame() {
    var contentFrame;
    var objFrameset;
    contentFrame = getTopWindow().findFrame(getTopWindow(), "content");
    objFrameset = contentFrame.document.getElementById("emxStructureNavigatorFrame");
    if (objFrameset != null) {
        objFrameset = objFrameset;
    } else {
        contentFrame = getTopWindow().findFrame(getTopWindow(), "treeContent");
        objFrameset = contentFrame.document.getElementById("emxStructureNavigatorFrame");
    }
    var cols = objFrameset.getAttribute("cols");
    var treeCols = parseInt(cols.split(",")[0]);
    while (treeCols > 0) {
        treeCols = treeCols - 10;
        setTimeout(function () {
            objFrameset.setAttribute("cols", treeCols + "0,*,0")
        }, 0);
    }
}


function openFrame() {
    var contentFrame;
    var objFrameset;
    var colCount;
    var treeColsLength = 200;
    contentFrame = getTopWindow().findFrame(getTopWindow(), "content");
    objFrameset = contentFrame.document.getElementById("emxStructureNavigatorFrame");
    if (objFrameset != null) {
        objFrameset = objFrameset;
    } else {
        contentFrame = getTopWindow().findFrame(getTopWindow(), "treeContent");
        objFrameset = contentFrame.document.getElementById("emxStructureNavigatorFrame");
    }
    if (getTopWindow().StructureNavigator) {
        colCount = getTopWindow().StructureNavigator.getCols();
        if (colCount != null) {
            treeColsLength = parseInt(colCount.split(",")[0]);
        }
    }
    var cols = objFrameset.getAttribute("cols");
    var treeCols = parseInt(cols.split(",")[0]);
    while (treeCols < treeColsLength) {
        treeCols = treeCols + 10;
        setTimeout(function () {
            objFrameset.setAttribute("cols", treeCols + ",*,0")
        }, 0);
    }

}

function isRowSelected(uiType) {
    return uiType === "table" ? (this.ids && this.ids.length > 1) : (isAnyRowSelectedInSB(this.oXML) && parent.ids && parent.ids.length > 1);
}


function hideMenu(e) {
    if (e.target.id == "GlobalNewTEXT") {
        jQuery('#AEFGlobalSearchHolder').css('display', 'none');
        e.stopPropagation();
        e.preventDefault();
        return;
    }
    jQuery('#AEFGlobalSearchHolder').css('display', 'none');
    e.stopPropagation();
    e.preventDefault();
    return;
}


var gTypeSea = "";
var gValSe = "";

function isSearchEnterKeyPressed(e) {
    jQuery('#AEFGlobalSearchHolder').css('display', 'none');
    var pK = "";
    if (typeof e != "undefined") {
        pK = document.all ? window.event.keyCode : e.which;
    }
    if (typeof e == "undefined" || pK == 13) {
        if (typeof e != "undefined") {
            e.preventDefault();
        }
        var queryType = emxUIConstants.STR_QUERY_TYPE;
        var gntext = document.getElementById("GlobalNewTEXT");
        var value1 = gntext.value;
        if (gntext.getAttribute("class") == null) {
            value1 = "";
        }

        var focus = false;
        if (value1 != null && value1 != "") {

            var typeName = jQuery('#AEFGlobalFullTextSearch')[0].getAttribute("typeName");
            var typeN = typeName != null ? typeName.replace("fromGlobalSearch", "") : null;
            if (!emxUIConstants.STR_REFRESH_WINDOW_SHADE) {
                if (gTypeSea != "" && gValSe != "" && gTypeSea == value1 && !emxUIConstants.STR_SUBMIT_GLOBAL_SEARCH && typeN != null) {
                    if (gValSe == typeN) {
                        showWindowShadeDialog(null, false, true);
                        return;
                    }
                }
            }
            var typetobePassed = "";
            var fromGlobalSearch = false;
            var showInitialResults = true;
            if (typeName != null && typeName != "" && typeName != "All_Search" && typeName.indexOf("fromGlobalSearch") < 0) {
                typetobePassed = "field=TYPES=" + encodeURIComponent(typeName);
            } else if (typeName != null && typeName != "" && typeName.indexOf("fromGlobalSearch") >= 0) {
                typeName = typeName.replace("fromGlobalSearch", "");
                //typeName = typeName.replace("showInitialResults=false","showInitialResults=true");
                if (typeName.indexOf("showInitialResults=false") >= 0) {
                    showInitialResults = false;
                }
                fromGlobalSearch = true;
            }

            if (validateBarChar(value1)) {
                if ((queryType == "Indexed" || showInitialResults) && !isValidSearchTextFTS(value1, parseInt(emxUIConstants.FTS_MINIUM_SEARCHCHARS))) {
                    return;
                }
                var value1 = value1.replace(/=/g, "E#Q#U#A#L");
                value1 = encodeURIComponent(value1);

                var dispalyStyle = document.getElementById('windowshade').style.display;
                gTypeSea = value1;
                var addURL = "";
                var reRunSearch = false;
                if (dispalyStyle.length != 0 && dispalyStyle != "none" && gValSe != "" && gValSe == typeN) {
                    reRunSearch = true;
                }
                if (queryType == "Indexed") {
                    if (fromGlobalSearch) {
                        var addURL = typeName + "&showInitialResults=true&txtTextSearch=" + value1 + "";
                    } else {
                        addURL = "../common/emxFullSearch.jsp?" + typetobePassed + "&table=AEFGeneralSearchResults&showInitialResults=true&genericDelete=true&selection=multiple&txtTextSearch=" + value1;
                    }
                } else {
                    if (fromGlobalSearch) {
                        addURL = typeName + "&showInitialResults=true&default=NAME=" + value1 + "&txtTextSearch=" + value1 + "";
                    } else {
                        addURL = "../common/emxFullSearch.jsp?" + typetobePassed + "&txtTextSearch=" + value1 + "&table=AEFGeneralSearchResults&showInitialResults=true&genericDelete=true&selection=multiple&default=TITLE=" + value1;
                    }
                }
                if (typeName != null)
                    gValSe = typeName.replace("fromGlobalSearch", "");
                ;
                addURL += "&fromCtxSearch=false";
                if (reRunSearch) {
                    var searchPageURL = addURL.substring(addURL.indexOf("?") + 1);
                    rerunsearch(value1, searchPageURL);
                } else {
                    showWindowShadeDialog(addURL, true, false);
                }
            } else {
                alert(BADCHAR_ENTERED + BAD_CHAR);
                focus = true;
            }

        } else {
            alert(FULL_SEARCH_ERROR);
            focus = true;
        }
        if (focus) {
            var objTextBox = document.getElementById("GlobalNewTEXT");
            objTextBox.value = "";
            objTextBox.setAttribute("class", "active");
            objTextBox.focus();
        }

    }
}

/* V6R2014 - To support the validation of the Full text search Free text
   To check the minium number fo characters entered/bad characters in the Free text search
*/
function isValidSearchTextFTS(searchText, minchars) {
    if (searchText.length > 0) {
        var txtVaal = searchText;
        var wildchar = false;
        if (searchText.indexOf("*") >= 0) {
            searchText = searchText.replace(/\*/g, '');
            wildchar = true;
        }
        if (searchText.indexOf("?") >= 0) {
            searchText = searchText.replace(/\?/g, '');
            wildchar = true;
        }
        if (searchText.length < minchars) {
            var stralert = emxUIConstants.STR_WILDCHARALERT;
            var strSearchAle = emxUIConstants.STR_WILDCHARCONFIRMSTR;
            var strConfirm = emxUIConstants.STR_WILDCHARCONFIRM;
            var strnonWildAle = emxUIConstants.STR_NONWILDCHARALERT;
            var strnonWildConfirm = emxUIConstants.STR_NONWILDCHARCONFIRM;

            strnonWildAle = strnonWildAle.replace(/{N}/, minchars);
            strnonWildAle = strnonWildAle.replace(/{ST}/, txtVaal);

            strnonWildConfirm = strnonWildConfirm.replace(/{N}/, minchars);
            strnonWildConfirm = strnonWildConfirm.replace(/{ST}/, txtVaal);
            strSearchAle = strSearchAle.replace(/{N}/, minchars);
            strSearchAle = strSearchAle.replace(/{ST}/, txtVaal);
            stralert = stralert.replace(/{N}/, minchars);
            stralert = stralert.replace(/{ST}/, txtVaal);
            strConfirm = strConfirm.replace(/{N}/, minchars);
            strConfirm = strConfirm.replace(/{ST}/, txtVaal);
            if (emxUIConstants.FTS_CHAR_VIOLATION == "true") {
                if (!wildchar) {
                    if (!confirm(strnonWildConfirm)) {
                        return false;
                    }
                } else if (!confirm(strConfirm)) {
                    return false;
                }
            } else {
                (wildchar == true) ? alert(strSearchAle) : alert(strnonWildAle);
                return false;
            }
        }

    }
    return true;
}


/* V6R2014 - To support the validation of the Full text search Free text */

function isValidTextSearchValueFTS(strFilterVal) {
    if (strFilterVal != null && strFilterVal != "") {
        var lenSearchTerms = strFilterVal.split(emxUIConstants.STR_REFINEMENT_SEPARATOR).length;
        if (lenSearchTerms != null && lenSearchTerms <= emxUIConstants.NUM_ALLOW_SEARCHTERMS) {
            var searchTerms = strFilterVal.split(emxUIConstants.STR_REFINEMENT_SEPARATOR);
            for (var i = 0; i < searchTerms.length; i++) {
                var searchTerm = searchTerms[i];
                if (searchTerm != null && searchTerm.length > emxUIConstants.NUM_LENGTH_SEARCHTERMS) {
                    alert(emxUIConstants.STR_SEARCHTERMS_INVAIDMESSAGE);
                    return false;
                }
            }
        } else {
            alert(emxUIConstants.STR_SEARCHTERMS_INVAIDMESSAGE);
            return false;
        }
    } else {
        alert(emxUIConstants.STR_SEARCHTERMS_INVAIDMESSAGE);
        return false;
    }
    return true;
}

/* V6R2014 - To support the Hide Refinement panel in the Full text searhc pages
 * User can able to hide the refienment panel to view search results in more AREA*/
function closeorOpenRefinments() {
    for (var i = 0; i < objMainToolbar.items.length; i++) {
        var iurl = objMainToolbar.items[i].url;
        if (iurl && iurl.indexOf("closeorOpenRefinments()") >= 0) {
            obj = objMainToolbar.items[i];
            break;
        }
    }
    if (obj) {
        if (obj.element.innerHTML.indexOf('iconActionClosePanel.gif') > 0) {
            toggleControlButton(obj, "iconActionClosePanel.gif", "iconActionOpenPanel.gif", emxUIConstants.STR_NAV_CLOSE, emxUIConstants.STR_NAV_OPEN, emxUIConstants.STR_CLOSE, emxUIConstants.STR_OPEN);
        } else if (obj.element.innerHTML.indexOf('iconActionOpenPanel.gif') > 0) {
            toggleControlButton(obj, "iconActionOpenPanel.gif", "iconActionClosePanel.gif", emxUIConstants.STR_NAV_OPEN, emxUIConstants.STR_NAV_CLOSE, emxUIConstants.STR_OPEN, emxUIConstants.STR_CLOSE);
        }
    }
    if (this.parent.jQuery('#searchPanel')[0].style.display == "" || this.parent.jQuery('#searchPanel')[0].style.display == "block") {
        this.parent.jQuery('#searchPanel')[0].style.display = "none";
        this.parent.jQuery('#searchBody')[0].style.display = "none";
        this.parent.jQuery('#refinementPanel')[0].style.display = "none";
        this.parent.jQuery('#divSearchHead')[0].style.left = "0px";
        this.parent.jQuery('#windowshade-content')[0].style.left = "0px";
    } else if (this.parent.jQuery('#searchPanel')[0].style.display == "none") {
        this.parent.jQuery('#searchPanel')[0].style.display = "block";
        if (this.parent.FullSearch.getFormBasedMode() == "true") {
            this.parent.jQuery('#searchBody')[0].style.display = "block";
        }
        if (this.parent.FullSearch.getFormBasedMode() == "false") {
            this.parent.jQuery('#refinementPanel')[0].style.display = "block";
            this.parent.jQuery('#leftNav')[0].style.width = "351px";
            this.parent.jQuery('#refinementPanel')[0].style.width = "351px";
        }
        this.parent.jQuery('#divSearchHead')[0].style.left = "351px";
        this.parent.jQuery('#windowshade-content')[0].style.left = "351px";
        this.parent.jQuery('#searchBody')[0].style.top = this.parent.jQuery('#searchHead')[0].offsetHeight + this.parent.jQuery('#searchOptions')[0].offsetHeight + "px";
        this.parent.jQuery('#leftNav')[0].style.top = this.parent.jQuery('#searchHead')[0].offsetHeight + this.parent.jQuery('#searchOptions')[0].offsetHeight + "px";
    }
}

/*
 * V6R2014 To initialise the customToolbar
 */
function initCustomToolbar(custToolbarJson) {
    if (!getTopWindow().emxUISlideIn) {
        return;
    }
    var slideIn = getTopWindow().emxUISlideIn.slidein_template("left");
    //$('div.facet.form div#customToolbarPlaceHolder',slideIn).remove();
    //slideIn[0].innerHTML = "";
    emxCustomToolbar._init(slideIn, custToolbarJson);
    if (jQuery('div#Refinement', slideIn).length == 0 && jQuery('div#tagnavigator', slideIn).length == 0 && slideIn.is(":visible")) {
        slideIn.hide();
    }
}

var emxCustomToolbar = {
    editModeCmds: new Array(), //Array to hold commands with mode as edit
    viewModeCmds: new Array(), //Array to hold commands with mode as view

    template: {
        facet_form: function (customDivName) {
            var custDiv = jQuery('<div name="custTLB" id="' + customDivName + '" class="facet form"></div>');
            custDiv.css('display', 'none');
            return custDiv;
        },
        facet_body: function (data) {
            var custname = data ? "id=" + data.name : "";
            return jQuery('<div class="facet-body" ' + custname + '></div>');
        },
        facet_li: function (data) {
            var uniqueId = emxCustomToolbar.seperateCmdsBySBMode(data);

            return jQuery('<li id=' + uniqueId + '></li>');
        },
        facet_label: function (obj) {
            var label = (obj.label) ? obj.label : "";
            var dispText = (obj.controlParams.displayText) ? obj.controlParams.displayText : "";
            return jQuery('<label title="' + label + '">' + dispText + '</label>');
        },
        facet_ul: function () {
            return jQuery('<ul></ul>');
        },
        facet_span: function (cls) {
            var strcls = cls ? "class=" + cls : "";
            return jQuery('<span ' + strcls + '></span>');
        },
        facet_button: function (obj) {
            var title = obj.controlParams.title;
            var button = jQuery('<button id="' + obj.name + '" name="' + obj.name + '" value = "' + obj.controlParams.actionLabel + '" title="' + title + '" url = "' + obj.controlParams.url + '"  target ="' + obj.controlParams.target + '" width="' + obj.controlParams.width + '" height="' + obj.controlParams.height + '" uiType="' + obj.controlParams.uiType + '" submitFunction="' + obj.controlParams.sf + '" submitProgram="' + obj.controlParams.sp + '" isModal="' + obj.controlParams.isModal + '" willSubmit="' + obj.controlParams.willSubmit + '"></button>');
            button.append(obj.controlParams.actionLabel);
            button.click(function (e) {
                var elem = e.target;
                emxCustomToolbar._submitToolbarForm(elem, null, "submit");
            });
            return button;
        },
        facet_chooserbutton: function (data) {
            var chooserbtn = jQuery('<button name="chooser" style="" chooserURL="' + data.controlParams.chooserOnClickURL + '" title="' + data.controlParams.title + '"></button>');
            jQuery(chooserbtn).click(function (e) {
                var elem = jQuery(e.target);
                eval(elem.attr("chooserURL"));
            });
            return chooserbtn;
        },
        facet_refresh: function () {
            var refresh = jQuery(this.facet_span("image-button"));
            var icon = jQuery('<img src="images/iconActionRefresh.gif">');
            refresh.append(icon);

            jQuery(refresh).click(function (e) {
                var elem = jQuery(e.target);
                var li = elem.closest('li');
                var textbox = jQuery("span input", li);

                var defvalue = textbox.attr('defaultSettingValue');

                var inputIds = [];
                for (var i = 0; i < textbox.size(); i++) {
                    textbox[i].value = defvalue;
                    inputIds.push(textbox[i].id);
                }

                var divPost = document.getElementById("divPostData");
                var inputs = divPost.getElementsByTagName("input");
                for (var j = 0; j < inputs.length; j++) {
                    var found = $.inArray(inputs[j].id, inputIds) > -1;
                    if (found) {
                        inputs[j].value = defvalue;
                    }
                }

            });
            return refresh;
        },
        facet_foot: function () {
            var foot = jQuery('<div class="facet-foot"><table><tr><td class="actions"></td></tr></table></div>');
            return foot;
        },
        facet_textbox: function (obj) {
            var title = obj.controlParams.title;
            var value = (obj.controlParams.defaultValue) ? obj.controlParams.defaultValue : "";
            var settingValue = (obj.controlParams.defaultSettingValue) ? obj.controlParams.defaultSettingValue : "";
            var format = obj.controlParams.format ? obj.controlParams.format : "";
            var commandCode = (obj.controlParams.commandCode) ? "sCommandCode = \"" + obj.name + "mxcommandcode" + "\"" : "";
            // To make the Textbox readonly for type/vault chooser and date
            var readOnly = "";
            if (format == "date" || (format == "chooser" && !(obj.controlParams.manualEdit == "true"))) {
                readOnly = "readonly";
            }
            var textBox = jQuery('<input id="' + obj.name + '" class="text" type="text" name="' + obj.name + '" value = "' + value + '" defaultValue="' + value + '" defaultSettingValue="' + settingValue + '" title="' + title + '" ' + readOnly + ' url = "' + obj.controlParams.url + '"  target ="' + obj.controlParams.target + '" width="' + obj.controlParams.width + '" height="' + obj.controlParams.height + '" uiType="' + obj.controlParams.uiType + '" submitFunction="' + obj.controlParams.sf + '" submitProgram="' + obj.controlParams.sp + '" isModal="' + obj.controlParams.isModal + '" willSubmit="' + obj.controlParams.willSubmit + '"' + commandCode + '" />');
            return textBox;
        },
        facet_checkbox: function (obj) {
            var title = obj.controlParams.title;
            var defaultValue = obj.controlParams.defaultValue;
            var checked = "";
            var commandCode = (obj.controlParams.commandCode) ? "sCommandCode = \"" + obj.name + "mxcommandcode" + "\"" : "";
            if (defaultValue && (defaultValue == "true" || defaultValue == "TRUE")) {
                checked = "checked";
            }
            var checkbox = jQuery('<input type="checkbox" id="' + obj.name + '" name="' + obj.name + '" value = "true" ' + checked + ' target ="' + obj.controlParams.target + '" width="' + obj.controlParams.width + '" height="' + obj.controlParams.height + '" uiType="' + obj.controlParams.uiType + '" submitFunction="' + obj.controlParams.sf + '" submitProgram="' + obj.controlParams.sp + '" isModal="' + obj.controlParams.isModal + '" willSubmit="' + obj.controlParams.willSubmit + '"' + commandCode + '" /><label title="' + title + '">' + obj.controlParams.displayText + '</label>');

            return checkbox;
        },
        facet_combobox: function (obj) {
            var title = obj.controlParams.title;
            var value = (obj.controlParams.defaultValue) ? obj.controlParams.defaultValue : "";
            var commandCode = (obj.controlParams.commandCode) ? "sCommandCode = \"" + obj.name + "mxcommandcode" + "\"" : "";
            var comboBox = jQuery('<select id="' + obj.name + '" name="' + obj.name + '" title="' + title + '" defaultValue="' + value + '" url = "' + obj.controlParams.url + '"  target ="' + obj.controlParams.target + '" width="' + obj.controlParams.width + '" height="' + obj.controlParams.height + '" uiType="' + obj.controlParams.uiType + '" submitFunction="' + obj.controlParams.sf + '" submitProgram="' + obj.controlParams.sp + '" isModal="' + obj.controlParams.isModal + '" willSubmit="' + obj.controlParams.willSubmit + '"' + commandCode + '"></select>');
            return comboBox;
        },
        facet_comboOptions: function (obj, combobox) {
            var title = obj.controlParams.title;
            var value = (obj.controlParams.defaultValue) ? obj.controlParams.defaultValue : "";
            for (var i = 0; i < obj.comboboxOptions.length; i++) {
                var fieldChoice = obj.comboboxOptions[i].fieldChoice ? obj.comboboxOptions[i].fieldChoice : "";
                var fieldChoiceDisplay = obj.comboboxOptions[i].fieldChoiceDisplay ? obj.comboboxOptions[i].fieldChoiceDisplay : "";
                var selected = ((fieldChoice == value) || fieldChoiceDisplay == (value)) ? "selected='selected'" : "";
                var op = jQuery('<option value="' + fieldChoice + '" ' + selected + '></option>"');
                op.append(fieldChoiceDisplay);
                combobox.append(op);
            }
            return combobox;
        },
        facet_submit: function (obj) {
            var uniqueId = emxCustomToolbar.seperateCmdsBySBMode(obj);

            var title = obj.controlParams.title;
            var actionLabel = obj.controlParams.actionLabel;
            title = (actionLabel) ? actionLabel : title;
            return jQuery('<button id="' + uniqueId + '" name="' + obj.name + '" class="apply" title="' + title + '" url="' + obj.controlParams.url + '"  target="' + obj.controlParams.target + '" width="' + obj.controlParams.width + '" height="' + obj.controlParams.height + '" uiType="' + obj.controlParams.uiType + '" submitFunction="' + obj.controlParams.sf + '" submitProgram="' + obj.controlParams.sp + '" isModal="' + obj.controlParams.isModal + '" willSubmit="' + obj.controlParams.willSubmit + '" sCommandCode = "' + obj.name + 'mxcommandcode"><span>' + title + '</span></button>');
        },
        drawCombobox: function (data) {
            var li = this.facet_li(data);
            var label = this.facet_label(data);
            li.append(label);
            var combobox = this.facet_combobox(data);
            combobox = this.facet_comboOptions(data, combobox);
            li.append(jQuery(this.facet_span("input").append(combobox)));

            jQuery(combobox).change(function (e) {
                var elem = e.target;
                emxCustomToolbar._submitToolbarForm(elem, null, "combobox");
            });

            return jQuery(li);
        },
        drawTextbox: function (data) {
            var actionLabel = data.controlParams.actionLabel;
            var url = data.controlParams.url;

            var li = this.facet_li(data);
            var label = this.facet_label(data);
            li.append(label);
            var textbox;
            // Button will  be displayed if url specified and setting Action Label is present
            if (emxCustomToolbar.isNotNullOrEmpty(actionLabel) && emxCustomToolbar.isNotNullOrEmpty(url)) {
                textbox = this.facet_textbox(data);
                var button = this.facet_button(data);
                var spanInput = this.facet_span("input");
                spanInput.append(textbox);
                spanInput.append(button);
                li.append(spanInput);
            } else {
                textbox = this.facet_textbox(data);
                li.append(jQuery(this.facet_span("input").append(textbox)));
            }


            var format = data.controlParams.format;

            if (format) {
                if (format == "chooser") {
                    var hiddenField = data.name + "_actualValue";
                    var hiddenFieldOID = data.name + "_OID";
                    var span = jQuery(this.facet_span());
                    span.append('<input id="' + hiddenField + '" name="' + hiddenField + '" type="hidden" value="' + data.controlParams.defaultActualValue + '" />');
                    span.append('<input id="' + hiddenFieldOID + '" name="' + hiddenFieldOID + '" type="hidden" value="' + data.controlParams.defaultOIDValue + '" />');
                    var button = jQuery(this.facet_chooserbutton(data));
                    span.append(button);
                    button.append("...");

                    li.append(span);
                }

                if (format == "date") {
                    /*showCalendar will take inputTime parameter to set the selected item in the calendar.
		              	The actual date value is read from the xml and passed to showCalendar method
		              	Start*/
                    var inputTime = data.controlParams.msValue;
                    if (inputTime) {
                        inputTime = new Number(inputTime);
                        var dateObj = new Date(inputTime);

                        var month = dateObj.getMonth() + 1;
                        var day = dateObj.getDate();
                        var year = dateObj.getFullYear();
                        var hours = dateObj.getHours();
                        var minutes = dateObj.getMinutes();
                        var seconds = dateObj.getSeconds();

                        var ampm = "AM";
                        if (new Number(hours) > 11 && new Number(hours) != 12) {
                            hours = hours - 12;
                            ampm = "PM";
                        }
                        inputTime = month + "/" + day + "/" + year + " " + hours + ":" + minutes + ":" + seconds + " " + ampm;
                    } else {
                        inputTime = "";
                    }
                    //End
                    //calendar form to show the calendar popup
                    var emxCalendarForm = jQuery('<form name="' + data.name + 'Form"/>');
                    emxCalendarForm.append(li.children());
                    li.children().remove();

                    var cal = jQuery("<a href=\"javascript:getTopWindow().showCalendar('" + data.name + "Form' , '" + data.name + "' , '" + inputTime + "' ,'' ,'' ,'')\"></a>");
                    var calImg = jQuery('<img src="../common/images/iconSmallCalendar.gif"  style="display:inline" border="0" valign="absmiddle" name = "img5">');
                    var span = jQuery(this.facet_span());
                    cal.append(calImg);
                    span.append(cal);
                    span.append('<input type="hidden" name="' + data.name + '_msvalue" value="' + data.controlParams.msValue + '" />');
                    emxCalendarForm.append(span);
                    li.append(emxCalendarForm);
                }

            }

            var showClearBtn = data.controlParams.showClearButton;
            if (emxCustomToolbar.isNotNullOrEmpty(showClearBtn) && showClearBtn == "true") {
                var refresh = jQuery(this.facet_refresh());
                li.append(refresh);
            }

//	        	if(data.controlParams.sCommandCode){
//	        		jQuery(textbox).change(function(e){
//	        			var elem = e.target;
//	        			getTopWindow().sb.submitToolbarForm('','','','','','','','','','','',jQuery(elem).attr("sCommandCode"));
//	        		});
//	        	}

            return jQuery(li);
        },
        drawSubmitbutton: function (data) {
            var submit = this.facet_submit(data);

            var typeRelFilter = data.controlParams.typeRelFilter;
            if ("true" === typeRelFilter) {
                jQuery(submit).click(function (e) {
                    var elem = e.target;
                    getTopWindow().filterApplied = true;
                    getTopWindow().sb.filterPage();
                });

            } else {
                jQuery(submit).click(function (e) {
                    emxCustomToolbar._submitToolbarForm(this, null, "submit");
                });
            }

            jQuery(submit).keypress(function (e) {
                var elem = e.target;
                this._keyPressEvent(e, elem, data.inpType);
            });

            return jQuery(submit);
        },
        drawCheckbox: function (data) {
            var li = this.facet_li(data);
            var label = this.facet_label(data);

            var span = this.facet_span();
            var checkbox = this.facet_checkbox(data);
            span.append(checkbox);

            var typeRelFilter = data.controlParams.typeRelFilter;
            if ("true" === typeRelFilter) {
                jQuery(checkbox).click(function (e) {
                    var elem = e.target;
                    getTopWindow().sb.checkOneSelected(elem);
                });

            } else {
                jQuery(checkbox).click(function (e) {
                    var elem = e.target;
                    emxCustomToolbar._submitToolbarForm(elem, null, "checkbox");
                });
            }

            var fs = jQuery('<fieldset></fieldset>');
            fs.append(span);
            li.append(fs);
            return li;
        }
    },//template

    _init: function (slideIn, custToolbarJson) {
        this._drawCustToolbar(slideIn, custToolbarJson);
    },
    _redrawCustomToolbar: function () {
        var customTlb = jQuery('<div></div>');
        var data;
        if (getTopWindow().custToolbarJson) {
            data = emxUICore.parseJSON(getTopWindow().custToolbarJson);
        }

        if (data) {
            for (i = 0; i < data.length; i++) {
                var toolbarItems = this.template.facet_ul();
                var form = jQuery(this.template.facet_form());
                var foot = jQuery(this.template.facet_foot());
                var body = jQuery(this.template.facet_body(data[i]));
                form.append(body.append(toolbarItems));
                form.append(foot);
                customTlb.append(form);
                this._drawCustToolbarItems(toolbarItems, foot, data[i]);
            }
        }
        return customTlb;
    },
    _drawCustToolbar: function (slideIn, custToolbarJson) {
        var data;
        if (custToolbarJson) {
            data = emxUICore.parseJSON(custToolbarJson);
        }
        var customDivName = "";
        for (var i = 0; i < data.length; i++) {
            if (i == 0) {
                customDivName = data[i].name;
            } else {
                customDivName += "|" + data[i].name;
            }
        }

        var custToolbarDiv = slideIn;

        var expr = "div[id=" + "\"" + customDivName + "\"" + "]";
        jQuery(expr, custToolbarDiv).remove();

        if (data) {
            for (i = 0; i < data.length; i++) {
                var toolbarItems = this.template.facet_ul();
                var form = jQuery(this.template.facet_form(customDivName));
                var foot = jQuery(this.template.facet_foot());
                var body = jQuery(this.template.facet_body(data[i]));
                form.append(body.append(toolbarItems));
                form.append(foot);
                custToolbarDiv.append(form);
                this._drawCustToolbarItems(toolbarItems, foot, data[i]);
            }
        }
    },
    _drawCustToolbarItems: function (toolbarItems, foot, data) {
        var command = data.items;
        for (j = 0; j < command.length; j++) {
            var inputType = command[j].inpType;
            var cmd;
            if ("textbox".toUpperCase() === inputType.toUpperCase()) {
                cmd = this.template.drawTextbox(command[j]);
            } else if ("combobox".toUpperCase() === inputType.toUpperCase()) {
                cmd = this.template.drawCombobox(command[j]);
            } else if ("submit".toUpperCase() === inputType.toUpperCase()) {
                cmd = this.template.drawSubmitbutton(command[j]);
            } else if ("checkbox".toUpperCase() === inputType.toUpperCase()) {
                cmd = this.template.drawCheckbox(command[j]);
            }

            if ("submit".toUpperCase() === inputType.toUpperCase()) {
                jQuery("td", foot).append(cmd);
            } else {
                toolbarItems.append(cmd);
            }
        }
    },
    _submitToolbarForm: function (elem, event, inpType) {
        var url = this.isNotNullOrEmpty(jQuery(elem).attr("url")) ? jQuery(elem).attr("url") : "";
        var target = this.isNotNullOrEmpty(jQuery(elem).attr("target")) ? jQuery(elem).attr("target") : "";
        var name = this.isNotNullOrEmpty(jQuery(elem).attr("name")) ? jQuery(elem).attr("name") : "";
        var width = this.isNotNullOrEmpty(jQuery(elem).attr("width")) ? jQuery(elem).attr("width") : "";
        var height = this.isNotNullOrEmpty(jQuery(elem).attr("height")) ? jQuery(elem).attr("height") : "";
        var uiType = this.isNotNullOrEmpty(jQuery(elem).attr("uiType")) ? jQuery(elem).attr("uiType") : "";
        var submitFunction = this.isNotNullOrEmpty(jQuery(elem).attr("submitFunction")) ? jQuery(elem).attr("submitFunction") : "";
        var submitProgram = this.isNotNullOrEmpty(jQuery(elem).attr("submitProgram")) ? jQuery(elem).attr("submitProgram") : "";
        var isModal = this.isNotNullOrEmpty(jQuery(elem).attr("isModal")) ? jQuery(elem).attr("isModal") : "";
        var willSubmit = this.isNotNullOrEmpty(jQuery(elem).attr("willSubmit")) ? jQuery(elem).attr("willSubmit") : "";
        var sCommandCode = this.isNotNullOrEmpty(jQuery(elem).attr("sCommandCode")) ? jQuery(elem).attr("sCommandCode") : "";
        inpType = this.isNotNullOrEmpty(inpType) ? inpType : "";

        var slideInData = "";
        var divSlideIn = jQuery('div#leftSlideIn', getTopWindow().document);
        var filtersInpElem = jQuery(':input', divSlideIn);
        for (i = 0; i < filtersInpElem.length; i++) {
            var inpElem = filtersInpElem.get(i);
            if (!jQuery(inpElem).is('button')) {
                if (inpElem.type && (inpElem.type == 'checkbox' || inpElem.type == 'radio')) {
                    slideInData += "&" + inpElem.name + "=" + inpElem.checked;
                } else {
                    slideInData += "&" + inpElem.name + "=" + inpElem.value;
                }
            }
        }

        getTopWindow().sb.submitToolbarForm(url, target, name, width, height, uiType, submitFunction, submitProgram, isModal, willSubmit, inpType, sCommandCode, slideInData, true);
    },
    isNotNullOrEmpty: function (strValue) {
        if (strValue != "undefined" && strValue != null && strValue != "null" && strValue != "" && strValue.length > 0) {
            return true;
        } else {
            return false;
        }

    },
    seperateCmdsBySBMode: function (data) {
        var uniqueId = "INP" + new Date().getTime();
        var mode = data.controlParams.mode;
        if (mode == "edit") {
            this.editModeCmds.push(uniqueId);
        } else if (mode == "view") {
            this.viewModeCmds.push(uniqueId);
        }
        return uniqueId;
    }
};

function loadDelegatedUIAddExists() {
    if (parent.parent.window.loadDelegatedUIAddExists) {
        parent.parent.window.loadDelegatedUIAddExists();
    }

}


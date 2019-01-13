$(function () {
    $('#copyViewUrl').popover();
    $('#copyScoreUrl').popover();
})

$("span.url").each(function () {
    $(this).text(location.origin + "/dashboard/" + $(this).text());
});
$("a.url").each(function () {
    $(this).attr("href", location.origin + "/dashboard/" + $(this).attr("href"));
})
$("#showScoreMode").click(
    function () {
        $(".viewMode").hide();
        $(".scoreMode").show();
    }
);
$("#showViewMode").click(
    function () {
        $(".viewMode").show();
        $(".scoreMode").hide();
    }
);

$("#copyScoreUrl").click(
    function () {
        copyToClipboard($("#scoreUrl").get(0));
        $("#copyScoreUrl").popover('show');
        setTimeout(function () {
                $("#copyScoreUrl").popover('hide');
            }, 3000)
    }
);


$("#copyViewUrl").click(
    function () {
        copyToClipboard($("#viewUrl").get(0));
        $("#copyViewUrl").popover('show');
        setTimeout(function () {
            $("#copyViewUrl").popover('hide');
            }, 3000)
    }
);



copyToClipboard = function (elem) {
    // create hidden text element, if it doesn't already exist
    var targetId = "_hiddenCopyText_";
    var isInput = elem.tagName === "INPUT" || elem.tagName === "TEXTAREA";
    var origSelectionStart, origSelectionEnd;
    if (isInput) {
        // can just use the original source element for the selection and copy
        target = elem;
        origSelectionStart = elem.selectionStart;
        origSelectionEnd = elem.selectionEnd;
    } else {
        // must use a temporary form element for the selection and copy
        target = document.getElementById(targetId);
        if (!target) {
            var target = document.createElement("textarea");
            target.style.position = "absolute";
            target.style.left = "-9999px";
            target.style.top = "0";
            target.id = targetId;
            document.body.appendChild(target);
        }
        target.textContent = elem.textContent;
    }
    // select the content
    var currentFocus = document.activeElement;
    target.focus();
    target.setSelectionRange(0, target.value.length);

    // copy the selection
    var succeed;
    try {
        succeed = document.execCommand("copy");
    } catch (e) {
        succeed = false;
    }
    // restore original focus
    if (currentFocus && typeof currentFocus.focus === "function") {
        currentFocus.focus();
    }

    if (isInput) {
        // restore prior selection
        elem.setSelectionRange(origSelectionStart, origSelectionEnd);
    } else {
        // clear temporary content
        target.textContent = "";
    }
    return succeed;
}

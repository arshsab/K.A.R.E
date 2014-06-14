var converter = new Showdown.converter();

var addElem = function (data) {
    var name = data.name.replace(/\W/g, '');
    var li = '<li class="result"' +  '" id="' + data.name + '">' +
        '<a class = "gitlink" href="https://github.com/' + data.name + 
        '""><img class="gitim" src="assets/github.png"></a>' +  
        '<a class = "searchlink" href="/results.html?search=' + 
        encodeURIComponent(data.name) + '"><img class="gitim" src="assets/search.png"></a>' + 
        '<a href="#" class="reslink" id="repo-' + data.name.replace(/\W/g, '') + '">' + data.name + '</a>' +
        '<div id = "info"><div class  = "dlink">' + data.description +  
        '</div><br><br><div class  = "dlink dleft">' + 
        '<i class="fa fa-angle-left"></i><i class="fa fa-angle-right"></i><b>' + data.language +
        '</b></div><div class  = "dlink dright">' +
        '<i class="fa fa-star"></i><b>' + data.stars +  
        '</b></div>'+
        '</div></li>';
    $("#results").append(li);
    $(".result").css("opacity", "1");
    $("#repo-" + name).on("click", function() {
        fetchReadme(data.name, name);
    });
};

function fetchReadme(repo, id) {
    $.getJSON("https://api.github.com/repos/" + repo + "/readme", function(json) {
        $("#readme").css("opacity", "1");
        $("#readme").html(converter.makeHtml(UTF8ArrToStr(base64DecToArr(json.content))));
    });
    console.log(id);
    $(".reslink").each(function() {
        $(this).css("color", "white");
    });
    $("#repo-" + id).css("color", "red");
}

/************************************
 utils
 ***********************************/

var getParams = function () {
    return document.location.search.replace(/(^\?)/, '').split('&').reduce(function (o, n) {
        n = n.split('=');
        o[n[0]] = n[1];
        return o;
    }, {});
};




/************************************
 base64
 ***********************************/

function b64ToUint6 (nChr) {
    return nChr > 64 && nChr < 91 ?
        nChr - 65
        : nChr > 96 && nChr < 123 ?
        nChr - 71
        : nChr > 47 && nChr < 58 ?
        nChr + 4
        : nChr === 43 ?
        62
        : nChr === 47 ?
        63
        :
        0;
}

function base64DecToArr (sBase64, nBlocksSize) {

    var sB64Enc = sBase64.replace(/[^A-Za-z0-9\+\/]/g, ""), nInLen = sB64Enc.length,
        nOutLen = nBlocksSize ? Math.ceil((nInLen * 3 + 1 >> 2) / nBlocksSize) *
            nBlocksSize : nInLen * 3 + 1 >> 2, taBytes = new Uint8Array(nOutLen);

    for (var nMod3, nMod4, nUint24 = 0, nOutIdx = 0, nInIdx = 0; nInIdx < nInLen; nInIdx++) {
        nMod4 = nInIdx & 3;
        nUint24 |= b64ToUint6(sB64Enc.charCodeAt(nInIdx)) << 18 - 6 * nMod4;
        if (nMod4 === 3 || nInLen - nInIdx === 1) {
            for (nMod3 = 0; nMod3 < 3 && nOutIdx < nOutLen; nMod3++, nOutIdx++) {
                taBytes[nOutIdx] = nUint24 >>> (16 >>> nMod3 & 24) & 255;
            }
            nUint24 = 0;

        }
    }
    return taBytes;
}

/**
 * @return {string}
 */
function UTF8ArrToStr (aBytes) {
    var sView = "";
    for (var nPart, nLen = aBytes.length, nIdx = 0; nIdx < nLen; nIdx++) {
        nPart = aBytes[nIdx];
        sView += String.fromCharCode(
                nPart > 251 && nPart < 254 && nIdx + 5 < nLen ? /* six bytes */
                /* (nPart - 252 << 32) is not possible in ECMAScript! So...: */
                (nPart - 252) * 1073741824 + (aBytes[++nIdx] - 128 << 24) + (aBytes[++nIdx] - 128 << 18) + (aBytes[++nIdx] - 128 << 12) + (aBytes[++nIdx] - 128 << 6) + aBytes[++nIdx] - 128
                : nPart > 247 && nPart < 252 && nIdx + 4 < nLen ? /* five bytes */
                (nPart - 248 << 24) + (aBytes[++nIdx] - 128 << 18) + (aBytes[++nIdx] - 128 << 12) + (aBytes[++nIdx] - 128 << 6) + aBytes[++nIdx] - 128
                : nPart > 239 && nPart < 248 && nIdx + 3 < nLen ? /* four bytes */
                (nPart - 240 << 18) + (aBytes[++nIdx] - 128 << 12) + (aBytes[++nIdx] - 128 << 6) + aBytes[++nIdx] - 128
                : nPart > 223 && nPart < 240 && nIdx + 2 < nLen ? /* three bytes */
                (nPart - 224 << 12) + (aBytes[++nIdx] - 128 << 6) + aBytes[++nIdx] - 128
                : nPart > 191 && nPart < 224 && nIdx + 1 < nLen ? /* two bytes */
                (nPart - 192 << 6) + aBytes[++nIdx] - 128
                : /* nPart < 127 ? */ /* one byte */
                nPart
        );
    }
    return sView;
}

/************************************
 Start
 ***********************************/

$(document).ready(function () {
    var query = decodeURIComponent(getParams()["search"]);
    document.title = query;
    $("#search-box").val(query);
    var arr = query.split("/");
    $.getJSON("/searchjson?owner=" + arr[0] + "&repo="  + arr[1], function (data) {
        if (data.length === 0) {
            window.location.href = "404.html";
        } else {
            for (var i = 0; i < data.length; i++) {
                addElem(data[i]);
            }

            fetchReadme(data[0].name, data[0].name.replace(/\W/g, ''));
        }
    });
});

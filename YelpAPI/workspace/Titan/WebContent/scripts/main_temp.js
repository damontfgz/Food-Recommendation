(function() {

    /**
   * Variables
   */
  var user_id = '1111';
  var user_fullname = 'John';
  var lng = -122.08;
  var lat = 37.38;
    
function init() {
    //register even listener, grab all buttons and assign listener
    document.querySelector('#login-btn').addEventListener('click', login);
    document.querySelector('#nearby-btn').addEventListener('click', loadNearbyItems);
    document.querySelector('#fav-btn').addEventListener('click', loadFavoriteItems);
    document.querySelector('#recommend-btn').addEventListener('click', loadRecommendedItems);

	validateSession();
    //onSessionValid({"user_id":"1111","name":"John Smith","status":"OK"})
}

/******************** Session ***********************/
    function validateSession() {
        onSessionInvalid(); //first hide all non related element and only display login form
        var url = './login';
        var req = JSON.stringify({});

        showLoadingMessage('Validating session...');

        ajax('GET', url, req, 
            function(res) {
            var result = JSON.parse(res);
            if (result.status === 'OK') {
                onSessionValid(result);
            }
        });
    }
    
    function onSessionValid(result) {
        user_id = result.user_id;
        user_fullname = result.name; 
        
       	document.querySelector('#login-form').style.display = 'none';
        var itemNav = document.querySelector('#item-nav');
        var itemList = document.querySelector('#item-list');
        var avatar = document.querySelector('#avatar');
        var welcomeMsg = document.querySelector('#welcome-msg');
        var logoutBtn = document.querySelector('#logout-link');
        
        //if login success, show all element and load near by items
        welcomeMsg.innerHTML = 'Welcome ' + user_fullname;
        
        /*hideElement(loginForm);*/
        showElement(itemNav);
        showElement(itemList);
        showElement(avatar);
        showElement(welcomeMsg);
        showElement(logoutBtn, 'inline-block');
        
        initGeoLocation();
    }

    function onSessionInvalid() {
        /*var loginForm = document.querySelector("#login-form");*/
		document.querySelector("#login-form").style.display = 'block';
        document.querySelector('#item-nav').style.display = 'none';
       	document.querySelector('#item-list').style.display = 'none';
        document.querySelector('#avatar').style.display = 'none';
        document.querySelector('#welcome-msg').style.display = 'none';
        document.querySelector('#logout-link').style.display = 'none';
        
		
        /*hideElement(itemNav);
        hideElement(itemList);
        hideElement(avatar);
        hideElement(welcomeMsg);
        hideElement(logoutBtn);*/
        
    }

    function hideElement(element) {
        element.style.dispaly = 'none';
    }

    function showElement(element, style) {
        element.style.display = style ? style : 'block';
    }
    
    function initGeoLocation() {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(onPositionUpdated, onLoadPositionFailed, {
            maximumAge:60000
            });
            showLoadingMessage('Loading your location...');
        } else {
            onLoadPositionFailed();
        }
    }
    
    function onPositionUpdated(position) {
        lat = position.coords.latitude;
        lng = position.coords.longitude;
        
        loadNearbyItems();
    }
    
    function onLoadPositionFailed() {
        console.warn('geo location is not available');
        getLocationFromIP();
    }
    
    function getLocationFromIP() {
        var url = 'http://ipinfo.io/json';
        var data = null;
        
        ajax('GET', url, data,
            function(res) {
            var result = JSON.parse(res);
            if ('loc' in result) {
                var loc = result.loc.split(',');
                lat = loc[0];
                lng = loc[1];
            } else {
                console.warn('Get location from IP fail');
            }
            loadNearbyItems();
        });
    }
    

/******************************** login **************************/
    function login() {
        var username = document.querySelector('#username').value;
        var password = document.querySelector('#password').value;
        
		password = md5(username + md5(password));

        var url = './login';
        var req = JSON.stringify({
            user_id : username,
            password: password
        });
        
        ajax('POST', url, req, 
            //success callback
            function(res) {
            var result = JSON.parse(res);
            if (result.status === 'OK') {
                onSessionValid(result);
            }
        },
            function() {
            showLoginError();
        }, true);
        
    }
    
    function showLoginError() {
        document.querySelector('#login-error').innerHTML = 'Invalid username or password';
    }
    
    function clearLoginError() {
        document.querySelector('#login-error').innerHTML = '';
    }
    

/********************************* Helper function ********************************/
    function activeBtn(btnId) {
        var btns = document.querySelectorAll('.main-nav-btn');
        for (var i = 0; i < btns.length; i++) {
            btns[i].className = btns[i].className.replace(/\bactive\b/, '');
        }
        var active = document.querySelector('#'+ btnId);
        active.className += ' active';
    }

    
    function showLoadingMessage(msg) {
        var itemList = document.querySelector('#item-list');
        itemList.innerHTML = '<p class="notice"><i class="fa fa-spinner fa-spin"></i>' + msg + '</p>';
    }

	  function showWarningMessage(msg) {
	    var itemList = document.querySelector('#item-list');
	    itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-triangle"></i> ' +
	      msg + '</p>';
	  }

	  function showErrorMessage(msg) {
	    var itemList = document.querySelector('#item-list');
	    itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-circle"></i> ' +
	      msg + '</p>';
	  }


function loadNearbyItems() {
    console.log('loadNearbyItems');
    activeBtn('nearby-btn');
    
    var url = './search';
    var params = 'user_id=' + user_id + '&lat=' + lat + '&lon=' + lng;
    console.log(url + '?' + params);
    var data = null;
    
    ajax('GET', url + '?' + params, data,
        function(res) {
        var items = JSON.parse(res);
        if (!items || items.length === 0) {
            showLoadingMessage('No near by items');
        } else {
            listItems(items);
        }
    }, function() {
        showErrorMessage('Connot laod nearby items.');
    });
}

function loadFavoriteItems() {
    activeBtn('fav-btn');
	var url = './history';
	var params = 'user_id=' + user_id;
	//var data = JSON.stringify({});
	var data = null
	
	ajax('GET', url + '?' + params, data, 
	function(res) {
		var items = JSON.parse(res);
		if (!items || items.length === 0) {
			showWarningMessage('No Favorite items');
		} else {
			listItems(items);
		}
	}, function() {
		showErrorMessage('Cannot load favorite items');
	});
}

function loadRecommendedItems() {
    activeBtn('recommend-btn');
	
	var url = './recommendation';
    var params = 'user_id=' + user_id + '&lat=' + lat + '&lon=' + lng;
    /*console.log(url + '?' + params);*/
    var data = null;
    
    ajax('GET', url + '?' + params, data,
        function(res) {
        var items = JSON.parse(res);
        if (!items || items.length === 0) {
            showWarningMessage('No recomendation items, please select favorite item first');
        } else {
            listItems(items);
        }
    }, function() {
        showErrorMessage('Connot load Recommendation items.');
    });
}
   
    /**
   * A helper function that creates a DOM element <tag options...>
   * @param tag
   * @param options
   * @returns {Element}
   */
    function $create(tag, options) {
        var element = document.createElement(tag);
        for (var key in options) {
            if (options.hasOwnProperty(key)) {
                element[key] = options[key];
            }
        }
        return element;
    }
    
    
    
 /**
   * AJAX helper
   *
   * @param method - GET|POST|PUT|DELETE
   * @param url - API end point
   * @param data - request payload data
   * @param successCallback - Successful callback function
   * @param errorCallback - Error callback function
   */
    function ajax(method, url, data, successCallback, errorCallback) {
        var xhr = new XMLHttpRequest();
        xhr.open(method, url, true);
        
        xhr.onload = function() {
            if (xhr.status === 200) {
                successCallback(xhr.responseText);
            } else if (xhr.status === 403){
				onSessionInvalid();
			} else {
                errorCallback();
            }
        };
        
        xhr.onerror = function() {
            console.error("The request couldn't be complete");
            errorCallback();
        }
        
        if (data === null) {
            xhr.send();
        } else {
            xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
            xhr.send(data);
        }
    }

	/*
	add or remove item to favorite 
	*/
	function changeFavoriteItem(item_id) {
		var li = document.querySelector('#item-' + item_id);
		var favIcon = document.querySelector('#fav-icon-' + item_id);
		var favorite = !(li.dataset.favorite === 'true');
        
        var url = './history';
        var req = JSON.stringify({
            user_id: user_id,
            favorite:[item_id]
        });
        var method = favorite ? 'POST' : 'DELETE';
        ajax(method, url, req, 
            function(res) {
            var result = JSON.parse(res);
            if (result.status === 'OK' || result.result === 'SUCCESS') {
				
                li.dataset.favorite = favorite;
                favIcon.className = favorite ? 'fa fa-heart' : 'fa fa-heart-o';
            }
        });
	}




  /**
   * List recommendation items base on the data received
   *
   * @param items - An array of item JSON objects
   */
    
    function listItems(items) {
        var itemList = document.querySelector('#item-list');
        itemList.innerHTML = '';
        
        for (var i = 0; i < items.length; i++) {
            addItem(itemList, items[i]);
        }
    }
    
    /**
   * Add a single item to the list
   *
   * @param itemList - The <ul id="item-list"> tag (DOM container)
   * @param item - The item data (JSON object)
   *
   <li class="item">
   <img alt="item image" src="https://s3-media3.fl.yelpcdn.com/bphoto/EmBj4qlyQaGd9Q4oXEhEeQ/ms.jpg" />
   <div>
   <a class="item-name" href="#" target="_blank">Item</a>
   <p class="item-category">Vegetarian</p>
   <div class="stars">
   <i class="fa fa-star"></i>
   <i class="fa fa-star"></i>
   <i class="fa fa-star"></i>
   </div>
   </div>
   <p class="item-address">699 Calderon Ave<br/>Mountain View<br/> CA</p>
   <div class="fav-link">
   <i class="fa fa-heart"></i>
   </div>
   </li>
   */
  function addItem(itemList, item) {
    var item_id = item.item_id;

    // create the <li> tag and specify the id and class attributes
    var li = $create('li', {
      id: 'item-' + item_id,
      className: 'item'
    });

    // set the data attribute ex. <li data-item_id="G5vYZ4kxGQVCR" data-favorite="true">
    li.dataset.item_id = item_id;
    li.dataset.favorite = item.favorite;
	//li.appendChild($create('img', {src: 'https://s3-media3.fl.yelpcdn.com/bphoto/EmBj4qlyQaGd9Q4oXEhEeQ/ms.jpg'}));
    // item image
    if (item.imgUrl) {
      li.appendChild($create('img', { src: item.imgUrl }));
    } else {
      li.appendChild($create('img', {
        src: 'https://assets-cdn.github.com/images/modules/logos_page/GitHub-Mark.png'
      }));
    }
    // section
    var section = $create('div');

    // title
    var title = $create('a', {
      className: 'item-name',
      href: item.url,
      target: '_blank'
    });
    title.innerHTML = item.storeName;
    section.appendChild(title);

    // category
    var category = $create('p', {
      className: 'item-category'
    });
    category.innerHTML = 'Category: ' + item.categories.join(', ');
    section.appendChild(category);

    // stars
    var stars = $create('div', {
      className: 'stars'
    });

    for (var i = 0; i < item.rating; i++) {
      var star = $create('i', {
        className: 'fa fa-star'
      });
      stars.appendChild(star);
    }

    if (('' + item.rating).match(/\.5$/)) {
      stars.appendChild($create('i', {
        className: 'fa fa-star-half-o'
      }));
    }

    section.appendChild(stars);

    li.appendChild(section);

    // address
    var address = $create('p', {
      className: 'item-address'
    });

    // ',' => '<br/>',  '\"' => ''
    address.innerHTML = item.address.replace(/,/g, '<br/>').replace(/\"/g, '');
    li.appendChild(address);

    // favorite link
    var favLink = $create('p', {
      className: 'fav-link'
    });

    favLink.onclick = function() {
      changeFavoriteItem(item_id);
    };

    favLink.appendChild($create('i', {
      id: 'fav-icon-' + item_id,
      className: item.favorite ? 'fa fa-heart' : 'fa fa-heart-o'
    }));

    li.appendChild(favLink);
    itemList.appendChild(li);
  }
      
      
      
      
      
      
    
init();

})();
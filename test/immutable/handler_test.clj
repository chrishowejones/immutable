(ns immutable.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [immutable.handler :refer :all]))

(deftest test-app
  (testing "main route"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 200))
      (is (re-find #"Person Demo App" (:body response)))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))


(deftest test-update-person
  (testing "get of update person page"
    (let [response (app (mock/request :get "/updateperson"))]
      (is (= (:status response) 200))
      (is (re-find #"Person" (:body response)))
      (is (re-find #"form" (:body response)))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))

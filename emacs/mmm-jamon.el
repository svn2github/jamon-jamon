;;; mmm-jamon.el --- MMM submode class for Jamon templates

; The contents of this file are subject to the Mozilla Public
; License Version 1.1 (the "License"); you may not use this file
; except in compliance with the License. You may obtain a copy of
; the License at http://www.mozilla.org/MPL/

; Software distributed under the License is distributed on an "AS
; IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
; implied. See the License for the specific language governing
; rights and limitations under the License.

; The Original Code is Jamon code, released February, 2003.

; The Initial Developer of the Original Code is Ian Robertson.
; Portions created by Ian Robertson are Copyright (C) 2003 Ian
; Robertson.  All Rights Reserved.

; Contributor(s): Jay Sachs

;;; Commentary:

;; This file contains the definition of an MMM Mode submode class for
;; editing Jamon templates.

;; To use this, put something like the following into your .emacs file:

;; (require 'mmm-jamon)
;; (add-to-list 'auto-mode-alist '("\\.jamon" . html-mode))
;; (mmm-add-mode-ext-class 'html-mode "\\.jamon" 'jamon)

;;; Code:

(require 'mmm-compat)
(require 'mmm-vars)
(require 'mmm-auto)

;;{{{ Java Tags

(defvar mmm-jamon-java-tags
  '("java" "class"))

(defvar mmm-jamon-java-tags-regexp
  (concat "<%" (mmm-regexp-opt mmm-jamon-java-tags t) ">")
  "Matches tags beginning Jamon sections containing Java code.
Saves the name of the tag matched.")

;;}}}
;;{{{ Add Classes

(mmm-add-group
 'jamon
 `((jamon-java
    :submode java
    :match-face (("<%java>" . mmm-code-submode-face)
                 ("<%class>" . mmm-class-submode-face))
    :front ,mmm-jamon-java-tags-regexp
    :back "</%~1>"
    :save-matches 1)
   (jamon-one-line
    :submode java
    :face mmm-code-submode-face
    :front "^%"
    :back "$")
   (jamon-inline
    :submode java
    :face mmm-output-submode-face
    :front "<% "
    :back "%>")))

;;}}}

(provide 'mmm-jamon)

;;; mmm-jamon.el ends here